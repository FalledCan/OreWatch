# OreWatch

**An admin tool for catching X-ray cheaters.** OreWatch makes the ores around you glow through walls. Use it to compare a player's mining path with where the ores really are.

> Only operators can use it (`orewatch.use`, default: op). Other players never see the glowing blocks.

## Features

- **Glowing ores**: each ore near you gets a glowing outline that shows through walls. Every ore type has its own color.
- **Visible only to you**: the glow is shown only to the admin who turned it on. Several admins can use it at once.
- **Always up to date**: the glow disappears as soon as an ore is mined or leaves your range.
- **10 ore types**: coal, copper, iron, gold (including nether gold), redstone, lapis, diamond, emerald, nether quartz and ancient debris.
- **Clickable chat menu**: run `/orewatch` and click to toggle each ore.
- **English / Japanese**: messages follow each player's game language automatically. You can also edit every message.
- **Lightweight**: nothing is saved to the world, unloaded chunks are skipped, and you can set a limit on how many blocks glow.
- **No dependencies.**

## Commands

`/xray` and `/ow` work as aliases of `/orewatch`.

| Command | Description |
|---|---|
| `/orewatch` | Open the toggle menu |
| `/orewatch <ore>` | Toggle one ore (`coal`, `copper`, `iron`, `gold`, `redstone`, `lapis`, `diamond`, `emerald`, `quartz`, `debris`) |
| `/orewatch all` | Show every ore |
| `/orewatch off` | Turn everything off |
| `/orewatch reload` | Reload the config and language files |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `orewatch.use` | op | Use `/orewatch` |
| `orewatch.reload` | op | Use `/orewatch reload` |

## Configuration

```yaml
# auto = follow each player's game language, or set ja / en
language: auto
# Search radius around the player (blocks)
radius: 16
# How often to rescan (ticks, 20 = 1 second)
update-interval: 20
# Maximum glowing blocks per player
max-displays: 1500
```

You can edit the messages in `plugins/OreWatch/lang/en.yml` and `ja.yml`.

## Requirements

- Spigot / Paper **1.19.4 or newer** (display entities were added in 1.19.4)
- Folia is not supported

---

## 日本語

**X-ray を使っているプレイヤーを見つけるための管理者用ツールです。** 周りの鉱石を壁越しに光らせて表示します。プレイヤーの掘り方と、実際の鉱石の位置を見比べるのに使えます。

- 使えるのは OP だけです（`orewatch.use`）。他のプレイヤーには発光が見えません。
- 鉱石ごとに発光の色が違います。掘ったり範囲外に出たりすると、発光はすぐ消えます。
- 対応している鉱石は10種類です。`/orewatch` を実行すると、クリックで切り替えられるメニューが出ます（`/xray` や `/ow` でも使えます）。
- メッセージは日本語と英語に対応しています。初期設定では、各プレイヤーのゲームの言語に合わせて自動で切り替わります。
- 動作環境は Spigot / Paper 1.19.4 以降です。
