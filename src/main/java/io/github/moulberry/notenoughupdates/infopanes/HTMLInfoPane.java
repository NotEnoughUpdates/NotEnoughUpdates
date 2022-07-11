/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.infopanes;

import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.filter.Encoder;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.model.WikiModel;
import info.bliki.wiki.tags.HTMLBlockTag;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.IgnoreTag;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.util.AllowEmptyHTMLTag;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class HTMLInfoPane extends TextInfoPane {
	private static final WikiModel wikiModel;

	private final int ZOOM_FACTOR = 2;
	private final int IMAGE_WIDTH = 400;
	private final int EXT_WIDTH = 100;

	private ResourceLocation imageTexture = null;
	private BufferedImage imageTemp = null;
	private int imageHeight = 0;
	private int imageWidth = 0;

	private float xMin = 0;
	private int mouseOffset = 0;
	private boolean selected = false;

	private static boolean hasAttemptedDownload = false;

	/*
	 * Creates a wiki model and sets the configuration to work with hypixel-skyblock wikia.
	 */
	static {
		Configuration conf = new Configuration();
		conf.addTokenTag("img", new HTMLTag("img"));
		conf.addTokenTag("code", new HTMLTag("code"));
		conf.addTokenTag("span", new AllowEmptyHTMLTag("span"));
		conf.addTokenTag("table", new HTMLBlockTag("table", Configuration.SPECIAL_BLOCK_TAGS + "span|"));
		conf.addTokenTag("infobox", new IgnoreTag("infobox"));
		conf.addTokenTag("tabber", new IgnoreTag("tabber"));
		conf.addTokenTag("kbd", new HTMLTag("kbd"));
		conf.addTokenTag("td", new AllowEmptyHTMLTag("td"));
		conf.addTokenTag("tbody", new AllowEmptyHTMLTag("tbody"));
		conf.addTokenTag("style", new AllowEmptyHTMLTag("style"));
		conf.addTokenTag("article", new AllowEmptyHTMLTag("article"));
		conf.addTokenTag("section", new AllowEmptyHTMLTag("section"));
		conf.addTokenTag("link", new AllowEmptyHTMLTag("link"));
		conf.addTokenTag("wbr", new AllowEmptyHTMLTag("wbr"));
		conf.addTokenTag("dl", new AllowEmptyHTMLTag("dl"));
		conf.addTokenTag("dd", new AllowEmptyHTMLTag("dd"));
		conf.addTokenTag("dt", new AllowEmptyHTMLTag("dt"));
		wikiModel = new WikiModel(conf, "https://hypixel-skyblock.fandom.com/wiki/Special:Filepath/${image}",
			"https://hypixel-skyblock.fandom.com/wiki/${title}"
		) {
			{
				TagNode.addAllowedAttribute("style");
				TagNode.addAllowedAttribute("src");
			}

			protected String createImageName(ImageFormat imageFormat) {
				String imageName = imageFormat.getFilename();
				if (imageName.endsWith(".svg")) {
					imageName += ".png";
				}
				imageName = Encoder.encodeUrl(imageName);
				if (replaceColon()) {
					imageName = imageName.replace(':', '/');
				}
				return imageName;
			}

			public void parseInternalImageLink(String imageNamespace, String rawImageLink) {
				rawImageLink = rawImageLink.replaceFirst("\\|x([0-9]+)px", "\\|$1x$1px");
				if (!rawImageLink.split("\\|")[0].toLowerCase().endsWith(".jpg")) {
					super.parseInternalImageLink(imageNamespace, rawImageLink);
				}
			}
		};
	}

	/**
	 * Takes a wiki url, uses NEUManager#getWebFile to download the web file and passed that in to #createFromWiki
	 */
	public static CompletableFuture<HTMLInfoPane> createFromWikiUrl(
		NEUOverlay overlay,
		NEUManager manager,
		String name,
		String wikiUrl
	) {
		return manager.getWebFile(wikiUrl).thenApply(f -> {
			if (f == null) {
				return new HTMLInfoPane(overlay, manager, "error", "error", "Failed to load wiki url: " + wikiUrl, false);
			}

			StringBuilder sb = new StringBuilder();
			try (
				BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), StandardCharsets.UTF_8))
			) {
				String l;
				while ((l = br.readLine()) != null) {
					sb.append(l).append("\n");
				}
			} catch (IOException e) {
				return new HTMLInfoPane(overlay, manager, "error", "error", "Failed to load wiki url: " + wikiUrl, false);
			}
			return createFromWikiText(
				overlay,
				manager,
				name,
				f.getName(),
				sb.toString(),
				wikiUrl.startsWith("https://wiki.hypixel.net/")
			);
		});
	}

	/**
	 * Takes raw wikia code and uses Bliki to generate HTML. Lot's of shennanigans to get it to render appropriately.
	 * Honestly, I could have just downloaded the raw HTML of the wiki page and displayed that but I wanted
	 * a more permanent solution that can be abstracted to work with arbitrary wiki codes (eg. moulberry.github.io/
	 * files/neu_help.html).
	 */

	private static final Pattern replacePattern = Pattern.compile(
		"<nav class=\"page-actions-menu\">.*</nav>|",
		Pattern.DOTALL
	);

	public static HTMLInfoPane createFromWikiText(
		NEUOverlay overlay, NEUManager manager, String name, String filename,
		String wiki, boolean isOfficialWiki
	) {
		if (isOfficialWiki) {
			wiki = wiki.split("<main id=\"content\" class=\"mw-body\">")[1].split("</main>")[0]; // hide top bar
			wiki = wiki.split("<div class=\"container-navbox\">")[0]; // hide giant bottom list
			wiki = wiki.split("<div class=\"categoryboxcontainer\">")[0]; // hide small bottom category thing
			wiki = replacePattern.matcher(wiki).replaceAll("");
			wiki = wiki.replaceAll(
				"<div id=\"siteNotice\"></div><div id=\"mw-dismissablenotice-anonplace\"></div><script>.*</script>",
				""
			); // hide beta box
			wiki = wiki.replaceAll("<h1 id=\"section_0\">.*</h1>", ""); // hide title
			wiki = wiki.replace("src=\"/", "src=\"https://wiki.hypixel.net/");
			wiki = wiki.replace("\uD83D\uDDF8", "✓"); // replace checkmark with one that renders
			wiki = wiki.replace("\uD83E\uDC10", "\u27F5"); // replace left arrow with one that renders
			wiki = wiki.replace("\uD83E\uDC12", "\u27F6"); // replace right arrow with one that renders
		} else {
			String[] split = wiki.split("</infobox>");
			wiki = split[split.length - 1]; //Remove everything before infobox
			wiki = wiki.split("<span class=\"navbox-vde\">")[0]; //Remove navbox
			wiki = wiki.split("<table class=\"navbox mw-collapsible\"")[0];
			wiki = "__NOTOC__\n" + wiki; //Remove TOC
		}
		try (PrintWriter out = new PrintWriter(new File(manager.configLocation, "debug/parsed.txt"))) {
			out.println(wiki);
		} catch (IOException ignored) {
		}
		String html;
		try {
			if (isOfficialWiki)
				html = wiki;
			else
				html = wikiModel.render(wiki);
		} catch (Exception e) {
			return new HTMLInfoPane(overlay, manager, "error", "error", "Could not render wiki.", false);
		}
		try (PrintWriter out = new PrintWriter(new File(manager.configLocation, "debug/html.txt"))) {
			out.println(html);
		} catch (IOException ignored) {
		}
		return new HTMLInfoPane(overlay, manager, name, filename, html, isOfficialWiki);
	}

	private String spaceEscape(String str) {
		return str.replace(" ", "\\ ");
	}

	private static final ExecutorService wkDownloadES = Executors.newSingleThreadExecutor();
	private static final ExecutorService rendererES = Executors.newCachedThreadPool();

	/**
	 * Uses the wkhtmltoimage command-line tool to generate an image from the HTML code. This
	 * generation is done asynchronously as sometimes it can take up to 10 seconds for more
	 * complex webpages.
	 */
	public HTMLInfoPane(
		NEUOverlay overlay,
		NEUManager manager,
		String name,
		String filename,
		String html,
		boolean isOfficial
	) {
		super(overlay, manager, name, "");
		this.title = name;

		String osId;
		if (SystemUtils.IS_OS_WINDOWS) {
			osId = "win";
		} else if (SystemUtils.IS_OS_MAC) {
			osId = "mac";
		} else if (SystemUtils.IS_OS_LINUX) {
			osId = "linux";
		} else {
			text = EnumChatFormatting.RED + "Unsupported operating system.";
			return;
		}

		File cssFile = new File(manager.configLocation, isOfficial ? "official-wiki.css" : "wikia.css");
		File wkHtmlToImage = new File(manager.configLocation, "wkhtmltox-" + osId + "/bin/wkhtmltoimage");

		//Use old binary folder
		if (new File(manager.configLocation, "wkhtmltox/bin/wkhtmltoimage").exists() && SystemUtils.IS_OS_WINDOWS) {
			wkHtmlToImage = new File(manager.configLocation, "wkhtmltox/bin/wkhtmltoimage");
		}

		Runtime runtime = Runtime.getRuntime();
		String[] chmodCommand = new String[]{
			"chmod", "-R", "777", new File(
			manager.configLocation,
			"wkhtmltox-" + osId
		).getAbsolutePath()
		};
		try {
			Process p = runtime.exec(chmodCommand);
			p.waitFor();
		} catch (IOException | InterruptedException ignored) {
		}

		if (!wkHtmlToImage.exists()) {
			if (hasAttemptedDownload) {
				text = EnumChatFormatting.RED + "Downloading web renderer failed? Or still downloading? Not sure what to do";
			} else {
				hasAttemptedDownload = true;
				Utils.recursiveDelete(new File(manager.configLocation, "wkhtmltox-" + osId));
				wkDownloadES.submit(() -> {
					try {
						File itemsZip = new File(manager.configLocation, "wkhtmltox-" + osId + ".zip");
						if (!itemsZip.exists()) {
							URL url = new URL("https://moulberry.codes/wkhtmltox/wkhtmltox-" + osId + ".zip");
							URLConnection urlConnection = url.openConnection();
							urlConnection.setConnectTimeout(15000);
							urlConnection.setReadTimeout(60000);

							FileUtils.copyInputStreamToFile(urlConnection.getInputStream(), itemsZip);
						}

						try (InputStream is = new FileInputStream(itemsZip)) {
							NEUManager.unzip(is, manager.configLocation);
						}

						itemsZip.delete();
						itemsZip.deleteOnExit();
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

				text = EnumChatFormatting.YELLOW + "Downloading web renderer... try again soon";
			}
			return;
		}

		if (!cssFile.exists() && isOfficial) {
			try {
				Files.copy(this.getClass().getResourceAsStream("/assets/notenoughupdates/official-wiki.css"), cssFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		File input = new File(manager.configLocation, "tmp/input.html");
		String outputFileName = filename.replaceAll("(?i)\\u00A7.", "")
																		.replaceAll("[^a-zA-Z0-9_\\-]", "_");
		File output = new File(manager.configLocation, "tmp/" +
			outputFileName + ".png");

		input.deleteOnExit();
		output.deleteOnExit();

		File tmp = new File(manager.configLocation, "tmp");
		if (!tmp.exists()) {
			tmp.mkdir();
		}

		if (output.exists()) {
			try {
				imageTemp = ImageIO.read(output);
				text = EnumChatFormatting.RED + "Creating dynamic texture.";
			} catch (IOException e) {
				e.printStackTrace();
				text = EnumChatFormatting.RED + "Failed to read image.";
				return;
			}
		} else {
			html = "<div id=\"mw-content-text\" lang=\"en\" dir=\"ltr\" class=\"mw-content-ltr mw-content-text\">" + html +
				"</div>";
			html = "<div id=\"WikiaArticle\" class=\"WikiaArticle\">" + html + "</div>";
			html = "<link rel=\"stylesheet\" href=\"file:///" + cssFile.getAbsolutePath().replaceAll("^/", "") + "\">\n" +
				html;

			try (
				PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(input), StandardCharsets.UTF_8)), false)
			) {

				out.println(encodeNonAscii(html));
			} catch (IOException ignored) {
			}

			try {
				text = EnumChatFormatting.GRAY + "Rendering webpage (" + name + EnumChatFormatting.RESET +
					EnumChatFormatting.GRAY + "), please wait...";

				String[] wkCommand = new String[]{
					wkHtmlToImage.getAbsolutePath(),
					"--width",
					"" + IMAGE_WIDTH * ZOOM_FACTOR,
					"--transparent",
					"--allow",
					manager.configLocation.getAbsolutePath(),
					"--zoom",
					"" + ZOOM_FACTOR,
					input.getAbsolutePath(),
					output.getAbsolutePath()
				};
				Process p = runtime.exec(wkCommand);
                /*Process p = runtime.exec(spaceEscape(wkHtmlToImage.getAbsolutePath()) + " --width "+
                        IMAGE_WIDTH*ZOOM_FACTOR+" --transparent --zoom "+ZOOM_FACTOR + " " + spaceEscape(input.getAbsolutePath()) +
                        " " + spaceEscape(output.getAbsolutePath()));*/
                /*Process p = runtime.exec("\""+wkHtmlToImage.getAbsolutePath() + "\" --width "+
                        IMAGE_WIDTH*ZOOM_FACTOR+" --transparent --zoom "+ZOOM_FACTOR+" \"" + input.getAbsolutePath() +
                        "\" \"" + output.getAbsolutePath() + "\"");*/
                /*Process p2 = runtime.exec("\""+wkHtmlToImage.getAbsolutePath() + "\" --width "+
                        (IMAGE_WIDTH+EXT_WIDTH)*ZOOM_FACTOR+" --transparent --zoom "+ZOOM_FACTOR+" \"" + input.getAbsolutePath() +
                        "\" \"" + outputExt.getAbsolutePath() + "\"");*/
				rendererES.submit(() -> {
					try {
						if (p.waitFor(15, TimeUnit.SECONDS)) {
							//if(p2.waitFor(5, TimeUnit.SECONDS)) {
							if (overlay.getActiveInfoPane() != this) return;

							try {
								imageTemp = ImageIO.read(output);
                                /*BufferedImage imageReg = ImageIO.read(output);
                                BufferedImage imageExt = ImageIO.read(outputExt);
                                ArrayList<Integer[]> pixels = new ArrayList<>();

                                int skip = IMAGE_WIDTH/EXT_WIDTH+1;

                                for(int y=0; y<imageReg.getHeight(); y++) {
                                    pixels.add(new Integer[IMAGE_WIDTH*ZOOM_FACTOR]);
                                    if(new Color(imageReg.getRGB(IMAGE_WIDTH*ZOOM_FACTOR-1, y), true).getAlpha() == 0) {
                                        for(int x=0; x<IMAGE_WIDTH*ZOOM_FACTOR; x++) {
                                            pixels.get(y)[x] = imageReg.getRGB(x, y);
                                        }
                                    } else {
                                        for(int x=0; x<(IMAGE_WIDTH+EXT_WIDTH)*ZOOM_FACTOR; x++) {
                                            int x2 = x*IMAGE_WIDTH/(IMAGE_WIDTH+EXT_WIDTH);
                                            int y2 = y*(IMAGE_WIDTH+EXT_WIDTH)/IMAGE_WIDTH;
                                            pixels.get(y)[x2] = imageExt.getRGB(x, y2);
                                        }
                                    }
                                }
                                imageTemp = new BufferedImage(IMAGE_WIDTH*ZOOM_FACTOR, pixels.size(), imageReg.getType());
                                for(int y=0; y<pixels.size(); y++) {
                                    for(int x=0; x<IMAGE_WIDTH*ZOOM_FACTOR; x++) {
                                        int col = pixels.get(y)[x];
                                        imageTemp.setRGB(x, y, col);
                                    }
                                }*/
								text = EnumChatFormatting.RED + "Creating dynamic texture.";
							} catch (IOException e) {
								e.printStackTrace();
								text = EnumChatFormatting.RED + "Failed to read image.";
								return;
							}
						} else {
							if (overlay.getActiveInfoPane() != this) return;

							text = EnumChatFormatting.RED + "Webpage render timed out (>15sec). Maybe it's too large?";
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
				text = EnumChatFormatting.RED + "Failed to exec webpage renderer.";
			}
		}
	}

	/**
	 * Renders a background, title and the image created in the ctor (if it has been generated).
	 */
	@Override
	public void render(
		int width,
		int height,
		Color bg,
		Color fg,
		ScaledResolution scaledresolution,
		int mouseX,
		int mouseY
	) {
		if (imageTemp != null && imageTexture == null) {
			DynamicTexture tex = new DynamicTexture(imageTemp);
			imageTexture = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
				"notenoughupdates/informationPaneImage", tex);
			imageHeight = imageTemp.getHeight();
			imageWidth = imageTemp.getWidth();
		}
		if (imageTexture == null) {
			super.render(width, height, bg, fg, scaledresolution, mouseX, mouseY);
			return;
		}

		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		int paneWidth = (int) (width / 3 * overlay.getWidthMult());
		int rightSide = (int) (width * overlay.getInfoPaneOffsetFactor());
		int leftSide = rightSide - paneWidth;

		int titleLen = fr.getStringWidth(title);
		fr.drawString(title, (leftSide + rightSide - titleLen) / 2, overlay.getBoxPadding() + 5, Color.WHITE.getRGB());

		drawRect(
			leftSide + overlay.getBoxPadding() - 5,
			overlay.getBoxPadding() - 5,
			rightSide - overlay.getBoxPadding() + 5,
			height - overlay.getBoxPadding() + 5,
			bg.getRGB()
		);

		int imageW = paneWidth - overlay.getBoxPadding() * 2;
		float scaleF = IMAGE_WIDTH * ZOOM_FACTOR / (float) imageW;

		Minecraft.getMinecraft().getTextureManager().bindTexture(imageTexture);
		GlStateManager.color(1f, 1f, 1f, 1f);
		if (height - overlay.getBoxPadding() * 3 < imageHeight / scaleF) {
			if (scrollHeight.getValue() > imageHeight / scaleF - height + overlay.getBoxPadding() * 3) {
				scrollHeight.setValue((int) (imageHeight / scaleF - height + overlay.getBoxPadding() * 3));
			}
			int yScroll = scrollHeight.getValue();

			float xSize = Math.min((paneWidth - overlay.getBoxPadding() * 2f) / imageWidth * scaleF, 1);
			float xMax = xMin + xSize;

			float vMin = yScroll / (imageHeight / scaleF);
			float vMax = (yScroll + height - overlay.getBoxPadding() * 3) / (imageHeight / scaleF);
			Utils.drawTexturedRect(leftSide + overlay.getBoxPadding(), overlay.getBoxPadding() * 2, imageW,
				(height - overlay.getBoxPadding() * 3),
				xMin, xMax, vMin, vMax
			);
			if (xSize < 1) {
				int barX = (int) (xMin * imageW) + leftSide + overlay.getBoxPadding();
				int barY = height - overlay.getBoxPadding() - 10;
				int barWidth = (int) (xMax * imageW) + leftSide + overlay.getBoxPadding();
				int barHeight = height - overlay.getBoxPadding() - 5;
				boolean isHovered = mouseX >= barX && mouseX <= barWidth && mouseY >= barY && mouseY <= barHeight || selected;
				Gui.drawRect(barX, barY, barWidth, barHeight, new Color(255, 255, 255, isHovered ? 150 : 100).getRGB());
			}
		} else {
			scrollHeight.setValue(0);

			Utils.drawTexturedRect(leftSide + overlay.getBoxPadding(), overlay.getBoxPadding() * 2, imageW,
				(int) (imageHeight / scaleF)
			);
		}
		GlStateManager.bindTexture(0);
	}

	@Override
	public boolean keyboardInput() {
		return false;
	}

	@Override
	public void mouseInput(int width, int height, int mouseX, int mouseY, boolean mouseDown) {
		int paneWidth = (int) (width / 3 * overlay.getWidthMult());
		int rightSide = (int) (width * overlay.getInfoPaneOffsetFactor());
		int leftSide = rightSide - paneWidth;
		int imageW = paneWidth - overlay.getBoxPadding() * 2;
		float scaleF = IMAGE_WIDTH * ZOOM_FACTOR / (float) imageW;
		float xSize = Math.min((paneWidth - overlay.getBoxPadding() * 2f) / imageWidth * scaleF, 1);
		float xMax = xMin + xSize;
		int barX = (int) (xMin * imageW) + leftSide + overlay.getBoxPadding();
		int barY = height - overlay.getBoxPadding() - 10;
		int barWidth = (int) (xMax * imageW) + leftSide + overlay.getBoxPadding();
		int barHeight = height - overlay.getBoxPadding() - 5;
		if (!mouseDown)
			selected = false;
		if (mouseX >= barX && mouseX <= barWidth && mouseY >= barY && mouseY <= barHeight && mouseDown || selected) {
			if (!selected)
				mouseOffset = mouseX - barX;
			xMin = (mouseX - leftSide - overlay.getBoxPadding() / 2f - mouseOffset) / imageWidth * scaleF;
			xMin = Math.max(0, xMin);
			xMin = Math.min(xMin, 1 - xSize);
			selected = true;
		}
		super.mouseInput(width, height, mouseX, mouseY, mouseDown);
	}

	//From https://stackoverflow.com/questions/1760766/how-to-convert-non-supported-character-to-html-entity-in-java
	public String encodeNonAscii(String c) {
		StringBuilder buf = new StringBuilder(c.length());
		CharsetEncoder enc = StandardCharsets.US_ASCII.newEncoder();
		for (int idx = 0; idx < c.length(); ++idx) {
			char ch = c.charAt(idx);
			if (enc.canEncode(ch))
				buf.append(ch);
			else {
				buf.append("&#");
				buf.append((int) ch);
				buf.append(';');
			}
		}
		return buf.toString();
	}
}
