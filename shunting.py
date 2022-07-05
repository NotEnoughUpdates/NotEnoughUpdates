precedence = {
    '-': 0,
    '+': 0,
    '*': 1,
    '/': 1,
}


def shunting(inss: [str]):
    op = []
    out = []
    for ins in inss:
        if str.isnumeric(ins):
            list.append(out, ins)
        elif len(ins) == 1 and ins in "+-*/":
            p = precedence[ins]
            while op:
                l = op[len(op) - 1]
                if l == "(":
                    break
                pl = precedence[l]
                if pl > p:
                    out.append(op.pop())
                else:
                    break
            op.append(ins)
        elif len(ins) == 1 and ins in "mkbt":
            out.append(ins)
        elif ins == "(":
            op.append(ins)
        elif ins == ")":
            while True:
                if not op:
                    raise "ILLEGAL PARENTHESIS"
                l = op.pop()
                if l == "(":
                    break
                out.append(l)
        else:
            raise f"UNKNOWN OP {ins}"
    while op:
        l = op.pop()
        if l == "(":
            raise "ILLEGAL PARENTHESIS"
        out.append(l)
    return out


print(shunting("1 + 22 k * ( 3 + 4 ) k".split()))
