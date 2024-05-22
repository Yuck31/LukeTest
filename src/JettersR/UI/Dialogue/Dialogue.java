package JettersR.UI.Dialogue;
/**
 * Author: Luke Sullivan
 * Last Edit: 3/22/2022
 */
import JettersR.Graphics.SpriteSheet;
import JettersR.Graphics.Sprite;
import JettersR.Graphics.Sprites;

public class Dialogue
{
    public static SpriteSheet dialogueSheet = Sprites.global_UISheet("DialogueSprites");
    public static Sprite[] dialogueSprites = dialogueSheet.loadLayout("DialogueSprites");
}
