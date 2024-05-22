package JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI;
/**
 * Author: Luke Sullivan
 * Last Edit: 11/25/2023
 */
import JettersR.Graphics.Screen;
import JettersR.Graphics.Animations.FrameAnimation_Timer;
import JettersR.Tiles.Graphics.TileAnimation;
import JettersR.Controller;
import JettersR.Game;
import JettersR.Level;
import JettersR.Mouse;
import JettersR.Graphics.Font;
import JettersR.Graphics.Fonts;
import JettersR.UI.Menus.Menu;
import JettersR.UI.ScrollBars.BasicScrollBar;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditor;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditorMain;

import static JettersR.Util.Fixed.*;

public class LE_Inspector extends Menu
{
	//Constants.
	public static final int WIDTH = 256, TILEANIM_CHOICE_HEIGHT = 60;
	private static final AAB_Box2D TILEANIM_CHOICE_BOX = new AAB_Box2D(WIDTH - 10, TILEANIM_CHOICE_HEIGHT, 0, 0);


	//Controller and Mouse for input.
	private Controller controller = Game.controller;
	private Mouse mouse = Game.mouse;

	//Level Editor.
	private LevelEditor levelEditor;
	private Level level;

	//Font.
	private Font arial, debugFont;

	//Tile Animations...?
	public static final int MAX_DELETE_TIMER = 60;
	private int deleteTimer = MAX_DELETE_TIMER;
	
	//Entity Field components go into the MenuComponents list.


	//Scroll Bars.
	private BasicScrollBar tileAnim_scrollBar, entField_scrollBar;


	/**Constructor.*/
	public LE_Inspector(LevelEditor levelEditor)
	{
		super(LevelEditorMain.WIDTH - WIDTH, 0, new AAB_Box2D(WIDTH, LevelEditorMain.HEIGHT, 0, 0));
		this.levelEditor = levelEditor;

		//Get font.
		arial = Fonts.get("Arial");
		debugFont = Fonts.get("Debug");

		//Create scroll bars.
		this.tileAnim_scrollBar = new BasicScrollBar
		(
			WIDTH - 4, LevelEditorMain.HEIGHT >> 1, 8, LevelEditorMain.HEIGHT,
			WIDTH, WIDTH >> 1, true, 16, LevelEditorMain.HEIGHT-2, 1,
			LevelEditor.SCROLL_HANDLE_COLOR_0, LevelEditor.SCROLL_HANDLE_COLOR_1, LevelEditor.SCROLL_BAR_COLOR
		);

		this.entField_scrollBar = new BasicScrollBar
		(
			WIDTH - 4, LevelEditorMain.HEIGHT >> 1, 8, LevelEditorMain.HEIGHT,
			WIDTH, WIDTH >> 1, true, 16, LevelEditorMain.HEIGHT-2, 1,
			LevelEditor.SCROLL_HANDLE_COLOR_0, LevelEditor.SCROLL_HANDLE_COLOR_1, LevelEditor.SCROLL_BAR_COLOR
		);
	}

	public void setLevel(Level level){this.level = level;}

	

	@Override
	public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
	{
		//Calculate offsets.
        int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);


		//
		//Up/Down input check.
		//
		boolean up = controller.menu_InputPressed(0, Controller.menu_UP, true),
		down = controller.menu_InputPressed(0, Controller.menu_DOWN, true);

		//Up
		if(up && !down)
		{
			currentChoice = ((currentChoice-1) + (levelEditor.tileSets.size()+1)) % (levelEditor.tileSets.size()+1);//Sets + 1 for Add New TileSet button.
			int currentScroll = (currentChoice * TILEANIM_CHOICE_HEIGHT);

			if(currentScroll < tileAnim_scrollBar.getLengthOffset()){tileAnim_scrollBar.setLengthOffset(currentScroll);}
			else if(currentScroll + TILEANIM_CHOICE_HEIGHT > tileAnim_scrollBar.getLengthOffset() + tileAnim_scrollBar.getVisableLength())
			{tileAnim_scrollBar.setLengthOffset((currentScroll + TILEANIM_CHOICE_HEIGHT) - tileAnim_scrollBar.getVisableLength());}

			//If holding delete, reset delete timer.
			deleteTimer = MAX_DELETE_TIMER;
		}
		//Down
		else if(down && !up)
		{
			currentChoice = (currentChoice+1) % (levelEditor.tileSets.size()+1);
			int currentScroll = (currentChoice * TILEANIM_CHOICE_HEIGHT);
			
			if(currentScroll < tileAnim_scrollBar.getLengthOffset()){tileAnim_scrollBar.setLengthOffset(currentScroll);}
			else if(currentScroll + TILEANIM_CHOICE_HEIGHT > tileAnim_scrollBar.getLengthOffset() + tileAnim_scrollBar.getVisableLength())
			{tileAnim_scrollBar.setLengthOffset((currentScroll + TILEANIM_CHOICE_HEIGHT) - tileAnim_scrollBar.getVisableLength());}

			//If holding delete, reset delete timer.
			deleteTimer = MAX_DELETE_TIMER;
		}

		//
		//Check for selection with the TileAnimations in view.
		//
		int first = tileAnim_scrollBar.getLengthOffset(), last = first + (tileAnim_scrollBar.getVisableLength());
		if(last > levelEditor.tileSets.size()){last = levelEditor.tileSets.size();}
		for(int i = first; i <= last; i++)
		{
			int ia = (i * TILEANIM_CHOICE_BOX.getHeight()) - tileAnim_scrollBar.getLengthOffset();

			//Collision check with box.
			boolean mouseIntersects = false;
			if(TILEANIM_CHOICE_BOX.intersects(mouse.getX(), mouse.getY(), xa, ya + ia))
			{
				mouseIntersects = true;
				if(Game.mouse.isMoving()){currentChoice = i;}
			}


			//TileSet bar.
			if(i < levelEditor.tileSets.size())
			{
				//Update the current TileSet_Bar. If confirmed...
				if(menuComponents.get(i).update(xa, ya + ia, currentChoice == i, false))
				{
					//TODO Open TileAnimation editor with this animation.
					
					//Don't update the rest of this menu.
					return true;
				}
			}
			//Add New TileAnimation.
			else if(input_Confirm_Pressed(0, mouseIntersects))
			{
				//TODO Show "Which type?" menu.
			}
		}

		return false;
	}

	@Override
	public void render(Screen screen, float xOffset, float yOffset)
	{
		//Calculate offsets.
        int xa = (int)(this.position.x + xOffset), ya = (int)(this.position.y + yOffset);
		
		//Render Background.
		//screen.fillRect(xa, ya, shape.getWidth(), shape.getHeight(), LevelEditor.scrollBarColor, false);

		//Render scroll bar.
		//if(levelEditor.tileSets.size() > 6){tileAnim_scrollBar.render(screen, xOffset, yOffset);}

		//Set crop region.
		screen.setCropRegion(xa+1, ya+1, xa + shape.getWidth()-1, ya + shape.getHeight()-1);

		//From current scroll to scroll extent, render TileAnimation choices.
		//int first = tileAnim_scrollBar.getLengthOffset() / TILEANIM_CHOICE_HEIGHT,
		//last = first + (tileAnim_scrollBar.getVisableLength() / TILEANIM_CHOICE_HEIGHT);
		//if(last > levelEditor.tileSets.size()){last = levelEditor.tileSets.size();}//Set to max because last option is to add a new TileAnimation.

		int first = 0, last = 0;

		//For every TileAnimation in view.
		for(int i = first; i <= last; i++)
		{
			//int ia = ((i * TILEANIM_CHOICE_HEIGHT) - tileAnim_scrollBar.getLengthOffset());
			int ia = (i * TILEANIM_CHOICE_HEIGHT);
			
			//Draw background.
			screen.fillRect(xa+1, ya + 1 + ia, TILEANIM_CHOICE_BOX.getWidth(), TILEANIM_CHOICE_BOX.getHeight(), LevelEditor.INSPECTOR_COLOR_0, false);

			if(i < level.getNumTileAnimations())
			{
				//Get current TileAnimation and its timer.
				TileAnimation ta = level.getTileAnimation(i);
				FrameAnimation_Timer timer = ta.getTimer();

				//Draw First TileMesh associated with this animation.
				ta.getTileMesh(0).render_2D(screen, xa+3, ya + 9 + ia);


				//
				//Draw information.
				//

				//ID.
				debugFont.render(screen, xa + 17, ya + 2 + ia, Integer.toString(i), false);

				//Current frame.
				arial.render(screen, xa + 44, ya + ia + 10, String.format("%03d", timer.getFrame()), false);
				arial.renderChar(screen, xa + 68, ya + ia + 10, '/', 1.0f, false);
				arial.render(screen, xa + 77, ya + ia + 10, String.format("%03d", timer.getNumFrames()-1), false);

				//Current frame's rate.
				arial.render(screen, xa + 44, ya + ia + 41, f_toString(timer.f_getTime()) + " / " + f_toString(timer.f_getFrameRate()), false);
				

				//TODO Number of TileMeshs associated.
				arial.render(screen, xa + 157, ya + ia + 10, ta.getNumTileMeshs() + " Bound", false);
			}
			else
			{
				//Draw "Create new Tile Animation."
				//arial.render(screen, xa + 5, ya + ia + 20, "Create new Tile Animation", false);
			}
		}

		//Reset crop region.
		screen.resetCropRegion();
	}
}
