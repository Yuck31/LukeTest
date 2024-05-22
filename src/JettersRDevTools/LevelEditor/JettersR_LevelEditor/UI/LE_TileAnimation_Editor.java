package JettersRDevTools.LevelEditor.JettersR_LevelEditor.UI;
/**
 * 
 */
import org.joml.Vector4f;

import JettersR.Game;
import JettersR.Controller;
import JettersR.Mouse;
import JettersR.Graphics.Screen;
import JettersR.Graphics.Animations.FrameAnimation_Timer;
import JettersR.Tiles.Graphics.*;
import JettersR.Graphics.Font;
import JettersR.Graphics.Fonts;
import JettersR.UI.Menus.Menu;
import JettersR.UI.Menus.MenuComponent;
import JettersR.UI.Menus.MenuChoices.MenuChoice;
import JettersR.UI.Menus.MenuChoices.MenuChoice_1P;
import JettersR.UI.Menus.TextBoxes.Rect_TextBox;
import JettersR.UI.ScrollBars.BasicScrollBar;
import JettersR.UI.Visuals.Labels.LabelLists.*;
import JettersR.UI.Visuals.Labels.LabelGrids.*;
import JettersR.Util.Shapes.Shapes2D.AAB_Box2D;
import JettersR.Util.Shapes.Shapes2D.Shape2D;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditor;
import JettersRDevTools.LevelEditor.JettersR_LevelEditor.LevelEditorMain;
import JettersR.Util.Annotations.fixed;

import static JettersR.Util.Fixed.*;

import java.util.Arrays;

public class LE_TileAnimation_Editor extends Menu
{
	public static final byte
	CHOICE_FRAME_RATES = 0,
	CHOICE_FRAME_ACTIONS = 1,
	CHOICE_FRAME_PARAMETERS = 2,
	CHOICE_ADD_NEW_FRAME = 3,
	CHOICE_USED_TILEMESHS = 4,
	CHOICE_DONE = 5,
	CHOICE_TILEBAR = 6;

	//LevelEditor.
	protected LevelEditor levelEditor;

	//Controller and mouse pointers.
    protected Controller controller;
    protected Mouse mouse;

	//Font.
    protected Font arial;


	//TileAnimation currently being edited.
	protected TileAnimation tileAnimation = null;

	//Rates column.
	protected @fixed int[] f_tileAnim_rates;
	protected final f_Int_LabelList f_ratesList;

	//Actions column.
	protected byte[] tileAnim_actions;
	private String[] tileAnim_actionNames;//TileAnimation action names, since they can be different with each type.
	protected final ByteAsString_LabelList actionsList;

	//TileAnimation parameters, since they can be of different data types.
	private float[][] color_actionParameters;
	private Color_ParamGrid color_paramGrid;
	//
	private short[][] spriteCoord_actionParameters;
	private Short_LabelGrid spriteCoord_paramGrid;
	//
	private byte[][] spriteIndex_actionParameters;
	private Byte_LabelGrid spriteIndex_paramGrid;
	//
	private @fixed short[][] f_wrap_actionParameters;
	private f_Short_LabelGrid f_wrap_paramGrid;

	//TextBox used for inputting values.
	private FrameValue_Textbox frameValue_textBox;

	//Add New Frame button.
	private MenuChoice_1P addNewFrame_button;
	private static final Vector4f ADD_NEW_FRAME_OUTTER_COLOR = Screen.intToVector4f(0xFF305B00);
	private static final Vector4f ADD_NEW_FRAME_INNER_COLOR = Screen.intToVector4f(0xFF60B500);

	//Scroll bar for frames.
	private BasicScrollBar frame_scrollBar;
	private static final int MAX_VISIBLE_FRAMES = 20;

	private int currentFrame = 0, currentParameter = 0;


	/**Constructor.*/
	public LE_TileAnimation_Editor(LevelEditor levelEditor)
	{
		super(0, 0, new AAB_Box2D(LevelEditorMain.WIDTH, LevelEditorMain.HEIGHT));
        this.controller = Game.controller;
        this.mouse = Game.mouse;

		//Set levelEditor.
		this.levelEditor = levelEditor;

        //Get Fonts.
        arial = Fonts.get("Arial");


		//Frame scroll bar. (made first because other components are dependent on it)
		frame_scrollBar = new BasicScrollBar
		(
			549, 210, 8, 380, 491, 491, true,
			1, MAX_VISIBLE_FRAMES, MAX_VISIBLE_FRAMES, LevelEditor.SCROLL_HANDLE_COLOR_0, LevelEditor.SCROLL_HANDLE_COLOR_1, LevelEditor.SCROLL_BAR_COLOR
		);

		//Textbox.
		frameValue_textBox = new FrameValue_Textbox();

		//TODO Make actions drop-down.


		//Rates column.
		f_ratesList = new f_Int_LabelList(null, arial);
		addComponent(new Frame_ListMenu(54, 20, new AAB_Box2D(RATE_BOX.getWidth(), RATE_BOX.getHeight(), 0, 0),
		RATE_BOX, RATE_BOX_OUTTER_COLOR, RATE_BOX_INNER_COLOR, f_ratesList, CHOICE_FRAME_RATES));

		//Actions column.
		actionsList = new ByteAsString_LabelList(null, null, arial);
		addComponent(new Frame_ListMenu(142, 20, new AAB_Box2D(ACTION_BOX.getWidth(), ACTION_BOX.getHeight(), 0, 0),
		ACTION_BOX, ACTION_BOX_OUTTER_COLOR, ACTION_BOX_INNER_COLOR, actionsList, CHOICE_FRAME_ACTIONS));

		//Parameters column.
		color_paramGrid = new Color_ParamGrid(null);
		spriteCoord_paramGrid = new Short_LabelGrid(null, arial);
		spriteIndex_paramGrid = new Byte_LabelGrid(null, arial);
		f_wrap_paramGrid = new f_Short_LabelGrid(null, arial);
		addComponent(new Parameters_ListMenu(297, 20));

		//Add New Frame button.
		addNewFrame_button = MenuChoice.basic_1P
		(
			299, 29, 491, 19, 
			() ->
			{
				//Increase rates by one slot.
				@fixed int[] f_newRates = new @fixed int[f_tileAnim_rates.length+1];
				for(int i = 0; i < f_tileAnim_rates.length; i++){f_newRates[i] = f_tileAnim_rates[i];}
				f_tileAnim_rates = f_newRates;
				f_ratesList.set(f_tileAnim_rates);
				tileAnimation.getTimer().f_setRates(f_tileAnim_rates);

				//Increase actions by one slot.
				byte[] newActions = new byte[tileAnim_actions.length+1];
				for(int i = 0; i < tileAnim_actions.length; i++){newActions[i] = tileAnim_actions[i];}
				tileAnim_actions = newActions;
				actionsList.setBytes(tileAnim_actions);
				tileAnimation.setActionIDs(tileAnim_actions);

				//Increase parameters by one slot.
				if(tileAnimation instanceof Color_TileAnimation)
				{
					color_actionParameters = Arrays.copyOf(color_actionParameters, color_actionParameters.length+1);
					color_actionParameters[color_actionParameters.length-1] = new float[Color_TileAnimation.getParamSize(0)];
					color_paramGrid.set(color_actionParameters);
					((Color_TileAnimation)tileAnimation).setActionParameters(color_actionParameters);
				}
				else if(tileAnimation instanceof SpriteCoord_TileAnimation)
				{
					spriteCoord_actionParameters = Arrays.copyOf(spriteCoord_actionParameters, spriteCoord_actionParameters.length+1);
					spriteCoord_actionParameters[spriteCoord_actionParameters.length-1] = new short[SpriteCoord_TileAnimation.getParamSize(0)];
					spriteCoord_paramGrid.set(spriteCoord_actionParameters);
					((SpriteCoord_TileAnimation)tileAnimation).setActionParameters(spriteCoord_actionParameters);
				}
				else if(tileAnimation instanceof SpriteIndex_TileAnimation)
				{
					spriteIndex_actionParameters = Arrays.copyOf(spriteIndex_actionParameters, spriteIndex_actionParameters.length+1);
					spriteIndex_actionParameters[spriteIndex_actionParameters.length-1] = new byte[SpriteIndex_TileAnimation.getParamSize(0)];
					spriteIndex_paramGrid.set(spriteIndex_actionParameters);
					((SpriteIndex_TileAnimation)tileAnimation).setActionParameters(spriteIndex_actionParameters);
				}
				else if(tileAnimation instanceof Wrap_TileAnimation)
				{
					f_wrap_actionParameters = Arrays.copyOf(f_wrap_actionParameters, f_wrap_actionParameters.length+1);
					f_wrap_actionParameters[f_wrap_actionParameters.length-1] = new @fixed short[Wrap_TileAnimation.getParamSize(0)];
					f_wrap_paramGrid.set(f_wrap_actionParameters);
					((Wrap_TileAnimation)tileAnimation).f_setActionParameters(f_wrap_actionParameters);
				}

				addNewFrame_button.position.y = 29 + (tileAnim_actions.length * (FRAMEVALUE_TEXTBOX_BOX.getHeight()));
			},
			new Vector4f[]{ADD_NEW_FRAME_OUTTER_COLOR, ADD_NEW_FRAME_INNER_COLOR}
		);
		addComponent(addNewFrame_button);
	}

	public void setTileAnimation(TileAnimation tileAnimation)
	{
		//Set TileAnimation to edit.
		this.tileAnimation = tileAnimation;

		//Get data from animation's timer.
		FrameAnimation_Timer timer = tileAnimation.getTimer();

		//Set rates.
		this.f_tileAnim_rates = timer.f_getRates();
		f_ratesList.set(f_tileAnim_rates);
		((Frame_ListMenu)menuComponents.get(CHOICE_FRAME_RATES)).updateBox();

		//Set actions and parameters.
		this.tileAnim_actions = tileAnimation.getActionIDs();
		Parameters_ListMenu paramsMenu = ((Parameters_ListMenu)menuComponents.get(CHOICE_FRAME_PARAMETERS));
		if(tileAnimation instanceof Color_TileAnimation)
		{
			tileAnim_actionNames = Color_TileAnimation.COLOR_ACTION_NAMES;
			color_actionParameters = ((Color_TileAnimation)tileAnimation).getActionParameters();
			color_paramGrid.set(color_actionParameters);
			paramsMenu.setGrid(color_paramGrid);
		}
		else if(tileAnimation instanceof SpriteCoord_TileAnimation)
		{
			tileAnim_actionNames = SpriteCoord_TileAnimation.SPRITE_COORD_ACTION_NAMES;
			spriteCoord_actionParameters = ((SpriteCoord_TileAnimation)tileAnimation).getActionParameters();
			spriteCoord_paramGrid.set(spriteCoord_actionParameters);
			paramsMenu.setGrid(spriteCoord_paramGrid);
		}
		else if(tileAnimation instanceof SpriteIndex_TileAnimation)
		{
			tileAnim_actionNames = SpriteIndex_TileAnimation.SPRITE_INDEX_ACTION_NAMES;
			spriteIndex_actionParameters = ((SpriteIndex_TileAnimation)tileAnimation).getActionParameters();
			spriteIndex_paramGrid.set(spriteIndex_actionParameters);
			paramsMenu.setGrid(spriteIndex_paramGrid);
		}
		else if(tileAnimation instanceof Wrap_TileAnimation)
		{
			tileAnim_actionNames = Wrap_TileAnimation.WRAP_ACTION_NAMES;
			f_wrap_actionParameters = ((Wrap_TileAnimation)tileAnimation).f_getActionParameters();
			f_wrap_paramGrid.set(f_wrap_actionParameters);
			paramsMenu.setGrid(f_wrap_paramGrid);
		}
		actionsList.setBytes(tileAnim_actions);
		actionsList.setStrings(tileAnim_actionNames);
		((Frame_ListMenu)menuComponents.get(CHOICE_FRAME_ACTIONS)).updateBox();
		paramsMenu.updateBox();


		//Move "Add New Frame" button.
		addNewFrame_button.position.y = 29 + (tileAnim_actions.length * FRAMEVALUE_TEXTBOX_BOX.getHeight());
	}


	@Override
	public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
	{
		if(isCurrentChoice)
		{
			//If not typing...
			if(!controller.isTyping())
			{
				//
				//Up/Down input check.
				//
				boolean up = controller.menu_InputPressed(0, Controller.menu_UP, true),
				down = controller.menu_InputPressed(0, Controller.menu_DOWN, true);

				if(currentChoice == CHOICE_ADD_NEW_FRAME)
				{
					if(up && !down)
					{
						currentChoice = CHOICE_FRAME_RATES;
						currentFrame = tileAnim_actions.length-1;
					}
					//Down
					else if(down && !up)
					{
						currentChoice = CHOICE_FRAME_RATES;
						currentFrame = 0;
					}
				}
				else if(currentChoice <= CHOICE_FRAME_PARAMETERS)
				{
					//Up
					if(up && !down)
					{
						//Move to "Add New Frame" button if up was pressed from the first frame.
						if(currentFrame <= 0)
						{
							currentChoice = CHOICE_ADD_NEW_FRAME;
							currentFrame = tileAnim_actions.length-1;
						}
						else{currentFrame--;}
						//currentFrame = ((currentFrame-1)+f_tileAnim_rates.length) % f_tileAnim_rates.length;

						if(currentFrame < frame_scrollBar.getLengthOffset()){frame_scrollBar.setLengthOffset(currentFrame);}
						else if(currentFrame + 1 > frame_scrollBar.getLengthOffset() + frame_scrollBar.getVisableLength())
						{frame_scrollBar.setLengthOffset((currentFrame + 1) - frame_scrollBar.getVisableLength());}
					}
					//Down
					else if(down && !up)
					{
						//Move to "Add New Frame" button if down was pressed from the last frame.
						if(currentFrame >= tileAnim_actions.length-1)
						{
							currentChoice = CHOICE_ADD_NEW_FRAME;
							currentFrame = 0;
						}
						else{currentFrame++;}
						//currentFrame = (currentFrame+1) % f_tileAnim_rates.length;
						
						if(currentFrame < frame_scrollBar.getLengthOffset()){frame_scrollBar.setLengthOffset(currentFrame);}
						else if(currentFrame + 1 > frame_scrollBar.getLengthOffset() + frame_scrollBar.getVisableLength())
						{frame_scrollBar.setLengthOffset((currentFrame + 1) - frame_scrollBar.getVisableLength());}
					}

					//Press Left Tab/Right Tab to switch to associated TileMeshs.
					if(controller.menu_InputPressed(0, Controller.menu_LEFT_TAB, true)
					|| controller.menu_InputPressed(0, Controller.menu_RIGHT_TAB, true))
					{currentChoice = CHOICE_USED_TILEMESHS;}

					((Frame_ListMenu)menuComponents.get(CHOICE_FRAME_RATES)).updateBox();
					((Frame_ListMenu)menuComponents.get(CHOICE_FRAME_ACTIONS)).updateBox();
					((Parameters_ListMenu)menuComponents.get(CHOICE_FRAME_PARAMETERS)).updateBox();
				}




				//
				//Left/Right input check.
				//
				switch(currentChoice)
				{
					case CHOICE_FRAME_RATES:
					{
						//Left, to parameters.
						if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
						{
							currentChoice = CHOICE_FRAME_PARAMETERS;
							currentParameter = currentFrame_paramLength() - 1;
						}

						//Right, to used actions.
						if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
						{currentChoice = CHOICE_FRAME_ACTIONS;}
					}
					break;

					case CHOICE_FRAME_ACTIONS:
					{
						//Left, to rates.
						if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
						{currentChoice = CHOICE_FRAME_RATES;}

						//Right, to parameters.
						if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
						{
							currentChoice = CHOICE_FRAME_PARAMETERS;
							currentParameter = 0;
						}
					}
					break;

					case CHOICE_FRAME_PARAMETERS:
					{
						//Parameters will be a little weird.
						//-Left moves left one parameter or to actions if on the leftmost parameter.
						//-Right moves right one parameter or to rates if on the rightmost parameter.

						//Left, to actions.
						if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
						{
							if(currentParameter <= 0){currentChoice = CHOICE_FRAME_ACTIONS;}
							else{currentParameter--;}
						}

						//Right, to rates.
						if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
						{
							if(currentParameter >= currentFrame_paramLength() - 1){currentChoice = CHOICE_FRAME_RATES;}
							else{currentParameter++;}
						}
					}
					break;

					case CHOICE_TILEBAR:
					{
						//Done in their own update functions.
					}
					break;

					//Used TileMeshs.
					case CHOICE_USED_TILEMESHS:
					{
						//Left, to Frame parameters.
						if(controller.menu_InputPressed(0, Controller.menu_LEFT, true))
						{currentChoice = CHOICE_FRAME_PARAMETERS;}

						//Right, to Frame rates.
						if(controller.menu_InputPressed(0, Controller.menu_RIGHT, true))
						{currentChoice = CHOICE_FRAME_RATES;}
						

						//Press confirm to go to the TileBar and add a TileMesh to be associated with the animation.
						//Hold special_0 to disassociate the TileMesh from the animation.
					}
					break;

					//Done button.
					case CHOICE_DONE:
					{
						//Up, to Frames.
						if(controller.menu_InputPressed(0, Controller.menu_UP, true))
						{
							//currentChoice = CHOICE_FRAMES;

							//Set frame choice to last.
						}
						else if(controller.menu_InputPressed(0, Controller.menu_DOWN, true))
						{
							//currentChoice = CHOICE_FRAMES;

							//Set frame choice to 0.
						}
					}
					break;
				}



			
				//Go through each component and update it.
				for(int i = 0; i < menuComponents.size(); i++)
				{
					//Cache the component.
					MenuComponent m = menuComponents.get(i);
					boolean mouseIntersects = false;

					//If not typing, check cursor collision with component.
					if(!controller.isTyping() && m.intersects(position.x + xOffset, position.y + yOffset, Game.mouse))
					{
						mouseIntersects = true;
						if(Game.mouse.isMoving())
						{
							//Set currentChoice.
							currentChoice = i;

							//Set frame column and row to -1.
							//if(currentChoice != CHOICE_TILESPRITE_SIDES)
							//{
								//tileSide_subMenu.currentColumn = -1;
								//tileSide_subMenu.currentRow = -1;
							//}
						}
					}

					//Update Menu component. Stop updating menu if confirmed.
					if(m.update(position.x + xOffset, position.y + yOffset, currentChoice == i, mouseIntersects)){return true;}
				}

				if(input_Cancel())
				{
					levelEditor.currentState = levelEditor.previousState;
				}
			}
			//If typing...
			else
			{
				frameValue_textBox.update(0, 0, true, false);
			}
		}

		return false;
	}

	private int currentFrame_paramLength()
	{
		return (tileAnimation instanceof Color_TileAnimation) ? color_actionParameters[currentFrame].length
		: (tileAnimation instanceof SpriteCoord_TileAnimation) ? spriteCoord_actionParameters[currentFrame].length
		: (tileAnimation instanceof SpriteIndex_TileAnimation) ? spriteIndex_actionParameters[currentFrame].length
		: (tileAnimation instanceof Wrap_TileAnimation) ? f_wrap_actionParameters[currentFrame].length
		: 0;
	}



	private static Vector4f DARKEN = Screen.intToVector4f(0x80000000);

	@Override
	public void render(Screen screen, float xOffset, float yOffset)
	{
		//Info we need to display.
		/*
		 * Type is decided on creation.
		 * 
		 * -TileMeshs associated with anim (which probably means we need to show the TileBar. Show IDs)
		 * -Each frame of the anim:
		 * --Time the frame lasts for (f_rates, so a fixed point number)
		 * --Action to perform on this frame
		 * --Parameters for said action.
		 */

		//Calculate offsets.
		float xa = this.position.x + xOffset, ya = this.position.y + yOffset;

		//Render components.
		for(int i = 0; i < menuComponents.size(); i++)
		{menuComponents.get(i).render(screen, xa, ya);}

		//Render scroll bar for frames.
		frame_scrollBar.render(screen, xOffset, yOffset);

		//Render textbox if needed.
		if(controller.isTyping())
		{
			//Darken the components rendered up to this point.
			screen.fillRect(0, 0, screen.getWidth(), screen.getHeight(), DARKEN, false);

			//Render the textBox.
			frameValue_textBox.render(screen, 0, 0);
		}
	}





	/*
     * Components used by the TileAnimation Editor.
     */
	private static final AAB_Box2D RATE_BOX = new AAB_Box2D(86, 19, 0, 0);
	private static final Vector4f RATE_BOX_OUTTER_COLOR = Screen.intToVector4f(0xFF3730A3),
	RATE_BOX_INNER_COLOR = Screen.intToVector4f(0xFF06005b);

	private static final AAB_Box2D ACTION_BOX = new AAB_Box2D(153, 19, 0, 0);
	private static final Vector4f ACTION_BOX_OUTTER_COLOR = Screen.intToVector4f(0xFF8B292F),
	ACTION_BOX_INNER_COLOR = Screen.intToVector4f(0xFF3D0004);

	private static final Vector4f HIGHLIGHT = new Vector4f(1.0f, 1.0f, 1.0f, 0.3f);

	public class Frame_ListMenu extends MenuComponent
	{
		//ListChoiceVisual so I can support bytes, float, even Sprites without writing much bloat.
		private UILabelList list;
		private AAB_Box2D frameBox;

		//Colors.
		private Vector4f outterColor, innerColor;

		private final byte choiceNum;

		/**Constructor.*/
		public Frame_ListMenu(int x, int y, Shape2D shape, AAB_Box2D frameBox, Vector4f outterColor, Vector4f innerColor, UILabelList list, byte choiceNum)
		{
			super(x, y, shape);
			this.frameBox = frameBox;

			this.outterColor = outterColor;
			this.innerColor = innerColor;
			this.list = list;

			this.choiceNum = choiceNum;
		}

		public void updateBox()
		{
			AAB_Box2D thisShape = (AAB_Box2D)this.shape;

			int lastVis = frame_scrollBar.getLengthOffset() + frame_scrollBar.getVisableLength();
			thisShape.setHeight((frame_scrollBar.getVisableLength() - (lastVis - list.getLength())) * frameBox.getHeight());
		}

		@Override
		public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
		{
			if(isCurrentChoice)
			{
				//Set current frame to mouse's position.
				if(mouseIntersectsThis && mouse.isMoving())
				{
					currentFrame = frame_scrollBar.getLengthOffset() +
					(int)((mouse.getY() - (position.y + yOffset)) / frameBox.getHeight());
				}

				//Confirm input check.
				if(input_Confirm_Pressed(0, mouseIntersectsThis))
				{
					//Perform repsonse for which column this is.
					switch(choiceNum)
					{
						case CHOICE_FRAME_RATES:
						{
							//Configure the textbox for a rate.
							frameValue_textBox.curChoice = currentChoice;
							frameValue_textBox.curFrame = currentFrame;
							frameValue_textBox.curParam = currentParameter;

							frameValue_textBox.position.x = (position.x + xOffset) + 1;
							frameValue_textBox.position.y = (position.y + yOffset) + (currentFrame * frameBox.getHeight()) + 1;

							frameValue_textBox.setTypingMode(Controller.TYPING_FLOAT);
							frameValue_textBox.setCharLimit(11);
							((AAB_Box2D)frameValue_textBox.getShape()).setWidth(86);
							frameValue_textBox.setText(f_toString(f_tileAnim_rates[currentFrame]));

							//Start typing.
							frameValue_textBox.beginTyping();
						}
						break;

						case CHOICE_FRAME_ACTIONS:
						{
							//Position actions_dropDown.
						}
						break;
					}
					
					//Don't update the rest of this menu.
					return true;
				}
			}

			return false;
		}

		@Override
		public void render(Screen screen, float xOffset, float yOffset)
		{
			screen.resetCropRegion();

			//Calculate offsets.
			int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

			//Render header rect.
			screen.fillRect(xa, ya - shape.getHeight(), shape.getWidth(), shape.getHeight(), innerColor, false);


			//Which choices are visible?
			int first = frame_scrollBar.getLengthOffset(), last = first + frame_scrollBar.getVisableLength();
			if(last >= f_tileAnim_rates.length){last = f_tileAnim_rates.length-1;}

			//Render the visible choices.
			for(int i = first; i <= last; i++)
			{
				int ia = (i - first) * frameBox.getHeight();

				//Render main visual.
				screen.drawRect(xa, ya + ia, frameBox.getWidth(), frameBox.getHeight(), outterColor, false);
				screen.fillRect(xa+1, ya+1 + ia, frameBox.getWidth()-2, frameBox.getHeight()-2, innerColor, false);

				if(currentChoice == this.choiceNum && currentFrame == i)
				{screen.fillRect(xa, ya + ia, frameBox.getWidth(), frameBox.getHeight(), HIGHLIGHT, false);}

				
				//Render label.
				list.render(screen, i, xa+2, ya+3 + ia);
			}
		}
	}



	private static final int PARAMETER_COLUMN_WIDTH = 248,
	PARAMETER_COLOR_BOX_WIDTH = 27,
	PARAMETER_BOX_WIDTH = 62;

	private static final Vector4f PARAMETER_BOX_OUTTER_COLOR = Screen.intToVector4f(0xFF2F8B29),
	PARAMETER_BOX_INNER_COLOR = Screen.intToVector4f(0xFF043D00);

	public class Parameters_ListMenu extends MenuComponent
	{
		//ListChoiceVisual so I can support bytes, float, even Sprites without writing much bloat.
		private UILabelGrid grid = null;
		private AAB_Box2D paramBox;

		//Colors.
		private Vector4f outterColor, innerColor;

		
		/**Constructor.*/
		public Parameters_ListMenu(int x, int y)
		{
			super(x, y, new AAB_Box2D(PARAMETER_COLUMN_WIDTH, ACTION_BOX.getHeight(), 0, 0));
			this.paramBox = new AAB_Box2D(PARAMETER_BOX_WIDTH, ACTION_BOX.getHeight(), 0, 0);

			this.outterColor = PARAMETER_BOX_OUTTER_COLOR;
			this.innerColor = PARAMETER_BOX_INNER_COLOR;
		}

		public void updateBox()
		{
			AAB_Box2D thisShape = (AAB_Box2D)this.shape;

			int lastVis = frame_scrollBar.getLengthOffset() + frame_scrollBar.getVisableLength();
			thisShape.setHeight((frame_scrollBar.getVisableLength() - (lastVis - grid.getLength())) * paramBox.getHeight());

			paramBox.setWidth((tileAnimation instanceof Color_TileAnimation) ? PARAMETER_COLOR_BOX_WIDTH : PARAMETER_BOX_WIDTH);
		}

		public void setGrid(UILabelGrid grid){this.grid = grid;}


		@Override
		public boolean update(float xOffset, float yOffset, boolean isCurrentChoice, boolean mouseIntersectsThis)
		{
			if(isCurrentChoice)
			{
				int numParams = grid.getLength(currentFrame);

				//Set current frame to mouse's position.
				if(mouseIntersectsThis && mouse.isMoving())
				{
					currentFrame = frame_scrollBar.getLengthOffset() +
					(int)((mouse.getY() - (position.y + yOffset)) / paramBox.getHeight());

					currentParameter = (int)((mouse.getX() - (position.x + xOffset)) / paramBox.getWidth());
					numParams = grid.getLength(currentFrame);
					if(currentParameter >= numParams){currentParameter = numParams-1;}
				}


				//Confirm input check.
				if(input_Confirm_Pressed(0, mouseIntersectsThis))
				{
					frameValue_textBox.curChoice = currentChoice;
					frameValue_textBox.curFrame = currentFrame;
					frameValue_textBox.curParam = currentParameter;

					frameValue_textBox.position.x = (position.x + xOffset) + (currentParameter * paramBox.getWidth()) + 1;
					frameValue_textBox.position.y = (position.y + yOffset) + (currentFrame * paramBox.getHeight()) + 1;

					//Configure the Textbox for whichever type of animation we're editing.
					if(tileAnimation instanceof Color_TileAnimation)
					{
						frameValue_textBox.setTypingMode(Controller.TYPING_INT);
						frameValue_textBox.setCharLimit(3);
						((AAB_Box2D)frameValue_textBox.getShape()).setWidth(27);
						frameValue_textBox.setText((int)(color_actionParameters[currentFrame][currentParameter] * 255));
					}
					else if(tileAnimation instanceof SpriteCoord_TileAnimation)
					{
						frameValue_textBox.setTypingMode(Controller.TYPING_INT);
						frameValue_textBox.setCharLimit(5);
						((AAB_Box2D)frameValue_textBox.getShape()).setWidth(62);
						frameValue_textBox.setText(spriteCoord_actionParameters[currentFrame][currentParameter]);
					}
					else if(tileAnimation instanceof SpriteIndex_TileAnimation)
					{
						frameValue_textBox.setTypingMode(Controller.TYPING_INT);
						frameValue_textBox.setCharLimit(3);
						((AAB_Box2D)frameValue_textBox.getShape()).setWidth(27);
						frameValue_textBox.setText(spriteIndex_actionParameters[currentFrame][currentParameter] & 0xFF);
					}
					else if(tileAnimation instanceof Wrap_TileAnimation)
					{
						frameValue_textBox.setTypingMode(Controller.TYPING_FLOAT);
						frameValue_textBox.setCharLimit(7);
						((AAB_Box2D)frameValue_textBox.getShape()).setWidth(62);
						frameValue_textBox.setText(f_toString(f_wrap_actionParameters[currentFrame][currentParameter]));
					}

					//Start typing.
					frameValue_textBox.beginTyping();
					
					//Don't update the rest of this menu.
					return true;
				}
			}

			return false;
		}

		@Override
		public void render(Screen screen, float xOffset, float yOffset)
		{
			screen.resetCropRegion();

			//Calculate offsets.
			int xa = (int)(position.x + xOffset), ya = (int)(position.y + yOffset);

			//Render header rect.
			screen.fillRect(xa, ya - paramBox.getHeight(), shape.getWidth(), paramBox.getHeight(), innerColor, false);


			//Which choices are visible?
			int first = frame_scrollBar.getLengthOffset(), last = first + frame_scrollBar.getVisableLength();
			if(last >= f_tileAnim_rates.length){last = f_tileAnim_rates.length-1;}

			//Render the visible choices.
			for(int i = first; i <= last; i++)
			{
				//Y offset from frameNum.
				int ia = (i - first) * paramBox.getHeight();

				//Get number of parameters and render underlying rect.
				int numParams = grid.getLength(i);
				screen.fillRect(xa+1, ya+1 + ia, (paramBox.getWidth() * numParams)-2, paramBox.getHeight()-2, innerColor, false);

				//Iterate through each parameter.
				for(int p = 0; p < numParams; p++)
				{
					int pa = (p * paramBox.getWidth());

					//Render bordor.
					screen.drawRect(xa + pa, ya + ia, paramBox.getWidth(), paramBox.getHeight(), outterColor, false);

					//Highight if needed.
					if(currentChoice == CHOICE_FRAME_PARAMETERS && currentFrame == i && currentParameter == p)
					{screen.fillRect(xa + pa, ya + ia, paramBox.getWidth(), paramBox.getHeight(), HIGHLIGHT, false);}

					//Render label.
					grid.render(screen, i, p, xa+2 + pa, ya+3 + ia);
				}
			}
		}
	}



	public class Color_ParamGrid implements UILabelGrid
	{
		//Shorts to render.
		private float[][] colorParams;

		//Font to render with.
		private Font font;
		private Vector4f fontColor;

		/**Constructor.*/
		public Color_ParamGrid(float[][] colorParams)
		{
			this.colorParams = colorParams;

			this.font = arial;
			this.fontColor = Screen.DEFAULT_BLEND;
		}

		public void set(float[][] colorParams){this.colorParams = colorParams;}
		
		public int getLength(){return colorParams.length;}
		public int getLength(int slotX){return colorParams[slotX].length;}

		@Override
		public void render(Screen screen, int slotX, int slotY, int xOffset, int yOffset)
		{
			font.render(screen, xOffset, yOffset, Integer.toString((int)(colorParams[slotX][slotY] * 255)), fontColor, false);
		}
	}




	public static final AAB_Box2D FRAMEROW_BOX = new AAB_Box2D(521, 32, 0, 0),
	FRAMEVALUE_TEXTBOX_BOX = new AAB_Box2D(125, 19, 0, 0);

	public static final Vector4f FRAMEVALUE_TEXTBOX_COLOR_0 = Screen.intToVector4f(0xFF284C00),
	FRAMEVALUE_TEXTBOX_COLOR_1 = Screen.intToVector4f(0xFF305B00);

	public class FrameValue_Textbox extends Rect_TextBox
	{
		public int curChoice, curFrame, curParam;

		/**Constructor.*/
		public FrameValue_Textbox()
		{
			super(0, 0, FRAMEVALUE_TEXTBOX_BOX, arial, Screen.DEFAULT_BLEND, 11,
			Controller.TYPING_FLOAT, 1.0f, FRAMEVALUE_TEXTBOX_COLOR_0, FRAMEVALUE_TEXTBOX_COLOR_1);
		}

		@Override
		public void valueSet()
		{
			if(curChoice == CHOICE_FRAME_RATES){f_tileAnim_rates[curFrame] = f_getText_Int();}
			else if(curChoice == CHOICE_FRAME_PARAMETERS)
			{
				if(tileAnimation instanceof Color_TileAnimation)
				{color_actionParameters[curFrame][curParam] = getText_Int() / 255.0f;}
				//
				else if(tileAnimation instanceof SpriteCoord_TileAnimation)
				{spriteCoord_actionParameters[curFrame][curParam] = getText_Short();}
				//
				else if(tileAnimation instanceof SpriteIndex_TileAnimation)
				{
					spriteIndex_actionParameters[curFrame][curParam] = getText_Byte();

					//byte value = getText_Byte();
					//if(value >= ){value = ;}
					//spriteIndex_actionParameters[curFrame][curParam] = value;
				}
				//
				else if(tileAnimation instanceof Wrap_TileAnimation)
				{f_wrap_actionParameters[curFrame][curParam] = (short)f_getText_Int();}
			}
		}
	}
}
