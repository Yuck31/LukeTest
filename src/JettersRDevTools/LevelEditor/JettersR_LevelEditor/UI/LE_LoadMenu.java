package JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI;
/**
 * 
 */
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import JettersR.Game;
import JettersR.Controller;
import JettersR.Mouse;
import JettersR.Graphics.Font;
import JettersR.Graphics.Fonts;
import JettersR.Graphics.Screen;
import JettersR.UI.Menus.Menu;
import JettersR.UI.Menus.MenuComponent;
import JettersR.UI.ScrollBars.BasicScrollBar;
import JettersR.UI.Visuals.Rect_DialogueBox;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditorMain;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditor;

public class LE_LoadMenu extends Menu
{
	public static Font font = Fonts.get("Arial");

	public class LE_LoadLevelChoice extends MenuComponent
	{
		private String text;
		private Rect_DialogueBox dialogueBox = null;

		private boolean selected = false;

		/**Constructor.*/
		public LE_LoadLevelChoice(int y, AAB_Box2D box, String text)
		{
			super(0, y, box);

			this.text = text;
			this.dialogueBox = new Rect_DialogueBox(MENU_WIDTH >> 1, y + (ELEMENT_HEIGHT >> 1), MENU_WIDTH, ELEMENT_HEIGHT, LevelEditor.MENU_COLOR_0);
		}

		@Override
		public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
		{
			//If this option is highlighted.
			if(isCurrentChoice)
			{
				//Change color of box.
				if(!selected)
				{
					selected = true;
					dialogueBox.setColor(LevelEditor.MENU_COLOR_1);
				}

				//Input check.
				if(input_Confirm_Pressed(mouseIntersectsThis))
				{
					//Set color back.
					dialogueBox.setColor(LevelEditor.MENU_COLOR_0);
					
					//Don't update the rest of the menu.
					return true;
				}
			}
			else if(selected)
			{
				selected = false;
				dialogueBox.setColor(LevelEditor.MENU_COLOR_0);
			}

			return false;
		}

		@Override
		public void render(Screen screen, float xOffset, float yOffset)
		{
			//Render box.
			dialogueBox.render(screen, xOffset, yOffset);

			//Render text.
			font.render(screen, (int)(position.x + xOffset) + 4, (int)(position.y + yOffset) + 1, text, Screen.DEFAULT_BLEND, false);
		}
	}

	//Dimension data.
	public static final int ELEMENT_HEIGHT = 16,
	MENU_WIDTH = 700, MENU_HEIGHT = 400,
	SCROLLBAR_WIDTH = 12;
	protected AAB_Box2D choiceBox = new AAB_Box2D(MENU_WIDTH, ELEMENT_HEIGHT);

	//Box for this menu.
	protected Rect_DialogueBox dialogueBox = null;

	//Scroll bar.
	private BasicScrollBar scrollBar;

	/**Constructor.*/
	public LE_LoadMenu()
	{
		super((LevelEditorMain.WIDTH - MENU_WIDTH) >> 1, (LevelEditorMain.HEIGHT - MENU_HEIGHT) >> 1, new AAB_Box2D(MENU_WIDTH, MENU_HEIGHT));

		this.dialogueBox = new Rect_DialogueBox(MENU_WIDTH >> 1, MENU_HEIGHT >> 1, MENU_WIDTH, MENU_HEIGHT, LevelEditor.MENU_COLOR_0);

		this.scrollBar = new BasicScrollBar
		(
			MENU_WIDTH - (SCROLLBAR_WIDTH >> 1), MENU_HEIGHT >> 1, SCROLLBAR_WIDTH, MENU_HEIGHT,
			MENU_WIDTH, MENU_WIDTH >> 1, true, 1, MENU_HEIGHT / ELEMENT_HEIGHT, 0,
			LevelEditor.SCROLL_HANDLE_COLOR_0, LevelEditor.SCROLL_HANDLE_COLOR_1, LevelEditor.SCROLL_BAR_COLOR
		);
	}

	//List of Files.
	private List<File> files = new ArrayList<File>();

	public void addOption(File folder)
	{
		files.add(folder);
		menuComponents.add(new LE_LoadLevelChoice(menuComponents.size() * ELEMENT_HEIGHT, choiceBox, folder.getName()));
	}

	public void calculateScrollBar()
	{
		//Bring scroll bar to top.
		scrollBar.setLengthOffset(0);

		//Set total length.
		scrollBar.setTotalLength(menuComponents.size());
	}

	@Override
	public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
	{
		if(isCurrentChoice)
        {
			//
			//Up/Down input check.
			//
			Controller controller = Game.controller;

			//Up and Down input checks.
			boolean up = controller.menu_InputPressed(0, Controller.menu_UP, true),
			down = controller.menu_InputPressed(0, Controller.menu_DOWN, true);

			//Up
			if(up && !down)
			{
				currentChoice = ((currentChoice-1)+menuComponents.size()) % menuComponents.size();

				if(currentChoice < scrollBar.getLengthOffset()){scrollBar.setLengthOffset(currentChoice);}
				else if(currentChoice > scrollBar.getLengthOffset() + scrollBar.getVisableLength())
				{scrollBar.setLengthOffset(currentChoice - scrollBar.getVisableLength());}
			}
			//Down
			else if(down && !up)
			{
				currentChoice = (currentChoice+1) % menuComponents.size();
				
				if(currentChoice < scrollBar.getLengthOffset()){scrollBar.setLengthOffset(currentChoice);}
				else if(currentChoice > scrollBar.getLengthOffset() + (scrollBar.getVisableLength() - ELEMENT_HEIGHT))
				{scrollBar.setLengthOffset(currentChoice - (scrollBar.getVisableLength() - ELEMENT_HEIGHT));}
			}


			//
			//Scroll bar check.
			//
			Mouse mouse = Game.mouse;

			//Update scroll bar.
			if(menuComponents.size() > scrollBar.getVisableLength())
			{scrollBar.update(xOffset, yOffset, false, scrollBar.intersects(xOffset, yOffset, mouse));}

			
			//The last element that can be seen.
			int offsetToElement = scrollBar.getLengthOffset() + scrollBar.getVisableLength(),
			limit = (offsetToElement >= menuComponents.size()) ? menuComponents.size() : offsetToElement;
	 
			//From offset to the last visable element.
			for(int i = scrollBar.getLengthOffset(); i < limit; i++)
            {
                //Cache the component.
                MenuComponent m = menuComponents.get(i);
                boolean mouseIntersects = false;

                //Collision check with choice.
                //if(m.intersects(position.x + xOffset, position.y + yOffset, Game.mouse))
				//Mouse Check.
				if(mouseIntersectsThis && m.intersects
				(
					this.position.x + xOffset,
					this.position.y + yOffset - (scrollBar.getLengthOffset() * ELEMENT_HEIGHT),
					mouse
				))
                {
                    mouseIntersects = true;
					if(Game.mouse.isMoving()){currentChoice = i;}
                }
                //System.out.println(currentChoice);

                //Update Menu Choice.
                if(m.update(position.x + xOffset, position.y + yOffset, currentChoice == i, mouseIntersects))
				{
					//Get outta here.
					return true;
				}
            }
        }
        //
        return false;
	}

	public File getFolder()
	{
		//Return selected folder.
		return files.get(currentChoice);
	}

	public void reset()
	{
		files.clear();
		menuComponents.clear();
	}

	@Override
	public void render(Screen screen, float xOffset, float yOffset)
	{
		int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

		//Set crop region.
		//screen.setCropRegion(xa, ya, xa + MENU_WIDTH, ya + MENU_HEIGHT);

		dialogueBox.render(screen, xa, ya);

		//Render choices.
		for(int i = 0; i < menuComponents.size(); i++)
        {menuComponents.get(i).render(screen, xa, ya);}

		//Reset crop region.
		//screen.resetCropRegion();

		//Render scroll bar.
		if(menuComponents.size() > scrollBar.getVisableLength()){scrollBar.render(screen, xa, ya);}
	}
}
