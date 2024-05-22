This project was uploaded to showcase an up-to-date look on my engine thus far. Be advised, it's buggy.

This project comes with:
-A base program showing a test level with a "revolving" global light with cel-shading and shadows.
 It also contains tiles using various different collision responses for testing purposes (some collisions are janky as a result).
--WASD to move.
--F to raise zPosition.
--E to increase speed while moving.
--G to snap to current tile.
--Numpad + to zoom in.
--Numpad - to zoom out.
--Numpad Enter to reset zoom.
--Arrow keys to control the second player. Some numpad keys next to them have the same functionally as player 1.

-A program made for editing animations in a custom format I use. (Needs full incorporation of fixed-point numbers)
--In layout view, press Enter to start regioning a sprite. Click where you want the upper-left and lower-right corners to be.
--Creating sprites will fill the bar on the left. Select a sprite by clicking on it (Janky. Will rewrite in due time).
--In anim view (click "ANIMATE" button), press Enter to add a frame using the selected sprite.
--S sets this frame's sprite to the one selected. R sets rate, X sets xOffset, Y sets yOffset, and A sets action ID (Softlocks. Will rewrite in due time).

-A WIP program intened to be a level editor.
--WASD to move camera.
--F/Left Click or G/Right Click to place selected tilemeshes. Click tiles in tilebar to select one. Scroll or drag scroll-bar to scroll.
  (If a tilemesh doesn't render, that's because that tile's collision-type is set to 0. WIP)
--While holding Q: W and S to zoom, F to reset zoom, and R and T to change active floor.
--While holding R: A and D to navigate the tilebar and F/G to select a tilemesh.
--While holding E: D to open tileAnimation editor, S to expand tilebar.
--Expanded TileBar: Click or Press F/G to select tiles, Q to enter TileMesh editor. ("Add New TileSet" button can crash. WIP)
