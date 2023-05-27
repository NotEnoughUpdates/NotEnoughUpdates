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

package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigAccordionId;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorAccordion;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorButton;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorText;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class ApiData {

	@Expose
	@ConfigOption(
		name = "Unlock the API data tab",
		desc = "If you turn this off, you will need to re-do the IQ test"
	)
	@ConfigEditorBoolean
	public boolean apiDataUnlocked = false;

	@Expose
	@ConfigOption(
		name = "Api Key",
		desc = "Hypixel API key\nYou can run §a/api new§r to autofill this value.",
		searchTags = "apikey"
	)
	@ConfigEditorText
	public String apiKey = "";

	@ConfigEditorAccordion(id = 0)
	@ConfigOption(name = "Repository", desc = "")
	public boolean repository = false;

	@Expose
	@ConfigOption(
		name = "Automatically Update Repository",
		desc = "Update the repository on every startup"
	)
	@ConfigEditorBoolean()
	@ConfigAccordionId(id = 0)
	public boolean autoupdate = true;

	@ConfigAccordionId(id = 0)
	@ConfigOption(
		name = "Update Repository now",
		desc = "Refresh your repository"
	)
	@ConfigEditorButton(runnableId = 22, buttonText = "Update")
	public int updateRepositoryButton = 0;

	@ConfigEditorAccordion(id = 1)
	@ConfigAccordionId(id = 0)
	@ConfigOption(
		name = "Repository Location",
		desc = ""
	)
	public boolean repositoryLocation = false;

	@ConfigAccordionId(id = 1)
	@ConfigOption(
		name = "Use default repository",
		desc = "The latest, most up to date item list for the official NEU releases."
	)
	@ConfigEditorButton(runnableId = 23, buttonText = "Reset")
	public int setRepositoryToDefaultButton = 0;

	@Expose
	@ConfigAccordionId(id = 1)
	@ConfigOption(
		name = "Repository User",
		desc = "Repository User"
	)
	@ConfigEditorText
	public String repoUser = "NotEnoughUpdates";

	@Expose
	@ConfigAccordionId(id = 1)
	@ConfigOption(
		name = "Repository Name",
		desc = "Repository Name"
	)
	@ConfigEditorText
	public String repoName = "NotEnoughUpdates-REPO";

	@Expose
	@ConfigAccordionId(id = 1)
	@ConfigOption(
		name = "Repository Branch",
		desc = "Repository Branch"
	)
	@ConfigEditorText
	public String repoBranch = "master";

	@Expose
	@ConfigAccordionId(id = 0)
	@ConfigOption(
		name = "Edit Mode",
		desc = "Enables you to edit items in the item list.\n§4Recommended for repository maintainers only.\n§4§lRemember: §rTurn off auto update as well"
	)
	@ConfigEditorBoolean
	public boolean repositoryEditing = false;

	@Expose
	@ConfigOption(
		name = "Lowestbin API",
		desc = "§4Do §lNOT §r§4change this, unless you know exactly what you are doing\n§fDefault: §amoulberry.codes"
	)
	@ConfigEditorText
	public String moulberryCodesApi = "moulberry.codes";

	public String getCommitApiUrl() {
		return String.format("https://api.github.com/repos/%s/%s/commits/%s", repoUser, repoName, repoBranch);
	}

	public String getDownloadUrl(String commitId) {
		return String.format("https://github.com/%s/%s/archive/%s.zip", repoUser, repoName, commitId);
	}

}
