# Shell Reborn

*Spoilers for the quest A Shell of a Friend.*

You found a lost snail on a little island in the Haunted Woods. You fed her, patched her up, gave her a name and
built her a shelter, and then you went off to fetch one last meal. You know the rest.

Shell Reborn brings her back. Once the quest is complete, she's home on her island again, pottering around near
the spot where you first met her.

## Features

- **She's back.** She appears on the island west of fairy ring ALQ (cross the stepping stone) after you finish the
  quest. Every so often she wanders a few tiles, using the game's own snail model, colours and animations.
- **She remembers her name.** Hovering over her shows the name you picked during the quest, read from the game's own
  record of your choice. You don't need to replay anything.
- **Examine her** to see how she's getting on. Each examine prints a random description in the chat, such as
  *"Sadie peeks out of her shelter, then sways in greeting. The leeches don't seem to bother her any more."*

## Settings

| Setting | What it does |
|---|---|
| Show the snail | Turns her on or off. |
| Only after the quest | Shows her only once A Shell of a Friend is complete. Turn it off to see her any time. |
| Name override | Leave it blank to use the name from the quest, or type any name. |
| Examine descriptions | The pool of descriptions, one per line. `{name}` becomes her name. Lines starting with `#` are skipped. |

## How it works

She's drawn by your client only. Nobody else can see her, and the plugin sends nothing to the game server.
Examining her is handled entirely inside RuneLite, so your character doesn't walk anywhere when you click her. She
stays out of the way while the quest is in progress, so she never doubles up with the quest's own snail.

## License

BSD 2-Clause. See [LICENSE](LICENSE).
