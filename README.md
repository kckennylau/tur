#tur

A state-based esoteric language that works like a turing machine, with much more features.

Inspired by Turing machine simulator (http://morphett.info/turing/turing.html).


#Usage

In cmd:

    > Tur.java "program_name.tur" "current_tape"

In Java:

    new Tur("code", "config", "tape").run();`

Alternatively:

    Tur t = new Tur();
    t.parse("code");
    t.setTape("tape");
    t.config("config");
    t.run();

Alternatively:

    Tur t = new Tur("code", "tape");
    t.config("config");
    t.run();

#The language

The tape is where the machine works at. It is initialized as an infinitude of spaces.

The code is divided into segments, which form the instructions of the machine.

Each segment contains 5 units, with some exceptions, which will be discussed later.

Each unit is usually 1 character long, with the following variations.

Single-quotation marks will include the next symbol, making the unit 2 characters long. Note that 'a is different from a.

Double-quotation marks always need to be paired, and can be arbitrarily long.

All whitespace characters will be ignored, except space which will not be ignored after a single-quotation mark or inside a pair of double-quotation marks.

<current_state> <current_symbol> <new_symbol> <new_direction> <new_state>

We shall use the following code for discussion:

    0 '_ '_ L 1
    0 '. '= R 0
    1 1 0 L 1
    1 0 1 H

This code can be compressed to `0'_'_L10'.'=R0110L1101H`, per above.

The tape is initialized as `110011`.

Initially, the machine is at state `0`, and the pointer is at the first `1`. The name of the states can be **anything**, using the above format (`a`, `'b`, `"cde"`). **The state `H` is reserved**.

The special-sequence `'.` means that it will match every character. A complete table of such special-sequences can be found below.

The special-sequence `'_` will match the space (U+0020).

The code is executed from top to bottom, so it will check whether the current symbol is a space first, before going to the second line.

So, in this case, the second line will be matched.

The second line: `0 '. '= R 0`

The third unit `'=` tells us to keep the symbol there and do not replace it.

The fourth unit `R` tells us to go to the right.

The fifth unit `0` tells us that the new state is now `0` (which is the original state anyway).

This is until it reaches the end of `110011`, where it is beyond `110011`, and matches a space (the tape is initialized to an infinitude of spaces).

Then the first line will be matched: `0 '_ '_ L 1`

The machine writes a space (`'_`), turns left (`L`), and goes to state `1`.

It then matches a `1` (the `1` at the end of `110011`): `1 1 0 L 1`

In that location, it writes a `0`, turns left (`L`), and goes back to state `1`.

The tape becomes `110010`.

It matches a `1` again, writes a `0`, turns left (`L`), and goes back to state `1`.

The tape becomes `110000`.

It matches `0`: `1 0 1 H`

It then writes a `1` (`110100`), and then halts.

#Exceptions

As demonstrated above, `1 0 1 H` **only have 4 units**, because it is a halt command.

There is another exception, where you can actually write something just before it halts. The state H is reserved for this purpose. For example, you would like to draw a smile if it halts at state 0. Then, you would write H 0 ":)" in the code. This is also the only instance where you can write more than one characters.

For reference, this is the code to detect whether a number in binary is divisible by 3 (the first picture in <https://en.wikipedia.org/wiki/Deterministic_finite_automaton>):

    00'_r0
    01'_r1
    10'_r2
    11'_r0
    20'_r1
    21'_r2
    H0":)"
    H'.":("

![deterministic finite state automata to check for divisibility by 3](https://en.wikipedia.org/wiki/File:DFA_example_multiplies_of_3.svg)

#The table of special sequences

special sequence | equivalent regex
--- | ---
`'d` | [0-9]
`'1` | [1-9]
`'2` | [01]
`'@` | [23456789]
`'3` | [012]
`'#` | [3456789]
`'4` | [0123]
`'$` | [456789]
`'5` | [01234]
`'%` | [56789]
`'6` | [012345]
`'^` | [6789]
`'7` | [0123456]
`'&` | [789]
`'8` | [01234567]
`'*` | [89]
`'9` | [012345678]
`'h` | [0-9a-f]
`'i` | [0-9A-F]
`'j` | [0-9a-fA-F]
`'w` | [a-zA-Z]
`'l` | [a-z]
`'u` | [A-Z]
`'a` | [0-9a-zA-Z]
`'b` | [_0-9a-zA-Z]
`'_` | matches a space
`'=` | do not change
`'.` | matches all (use it as a catch-all for the exceptions)

Use the uppercases to match the complement of the lowercases. For example, 'D would be [^0-9] since 'd is [0-9].

As can be seen, the second and third units can be regular expressions. However, if they only have character, it is always treated literally.

If the second unit is 'd ([0123456789]) and the third unit is "abcdefghij", then 0 will be converted to a, etc.

If there is not enough, the last character is recycled. It means that if the third unit is "abc" instead, it will be equivalent to "abcccccccc", so 7 will be matched to c.

Thus, the ROT13 is as follows:

    0 'u "N-ZA-M" R 0
    H 0 ":)"

Compressed to become `0'u"N-ZA-M"R0H0":)"`

**(If it fails to match any line, it will halt.)**

#Stacks

This language also comes with a stack, as well as a clipboard that is initialized to a space.

special sequence | equivalent regex
--- | ---
`'x` | cut (replace current symbol with space after copying it to the clipboard)
`'c` | copy (do not change current symbol, and copy to clipboard)
`'v` | paste (paste from clipboard)
 | 
`',` | push into stack without destroying the current symbol
`'.` | pop from a stack, replacing the current symbol (halts if stack is empty)
`';` | duplicate without destroying the current symbol
`':` | duplicate and pop to pointer location
`'\` | swap the first two symbols in the stack without destroying current symbol
`'/` | swap the first two symbols, then pop to replace current symbol
`'@` | rotate the first three symbols so that the third-from-top becomes top, without destroying current symbol
`'#` | do the same but replacing the current symbol

#Bonus

There is a hidden undocumented feature. Go find it! :)

#Bug report

Please do so in <https://github.com/kckennylau/tur/issues/new>.