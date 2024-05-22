package JettersR.UI;
/**
 * Manages UI Elements.
 * 
 * Author: Luke Sullivan.
 * Last Edit: 1/14/2023
 */
import java.util.ArrayList;
import java.util.List;

public class UIManager
{
    private List<UIComponent> components = new ArrayList<UIComponent>();

    public UIManager()
    {
        
    }

    public void clear()
    {
        //Iterator<UIComponent> iterator = components.iterator();
        //
        //while(iterator.hasNext())
        //{
            //iterator.next();
            //iterator.remove();
        //}

        components.clear();
    }
}
