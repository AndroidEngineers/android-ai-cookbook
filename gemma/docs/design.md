# PocketStories design

Direction: a pocket-sized adventure book, with tactile paper colors and moonlit illustrated scenes.

- Fraunces at weight 500 for expressive headings and story reading; DM Sans for controls and supporting copy. Both bundled, with licenses.
- Warm paper `#F7F2E9`, ink plum `#33263B`, lantern gold `#E9C580`, moss `#38574B`; light theme is fixed regardless of the device appearance preference.
- Original robot/key/mug-tower illustration, bundled at build time. Do not describe it as user-generated model output.
- Rounded book covers, generous whitespace, clear action hierarchy and a short, deliberate camera capture flow.
- Screens: Bookshelf, genre/title setup, camera/photo picker, reader with draft review, model import.
- Long content scrolls; main interactions use at least 48 dp target areas. Shake has an equivalent button.
- No simulated model generation. The starter story is hand-written and labeled on the bookshelf and reader.

Future designs: editable discovery cards, inventory drawer, branch timeline and PDF export. They must not appear as enabled actions until implemented.
