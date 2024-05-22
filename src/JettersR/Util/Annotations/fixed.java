package JettersR.Util.Annotations;
/**
 * 
 */
import java.lang.annotation.*;

//import javax.lang.model.element.ElementKind;

@Retention(RetentionPolicy.SOURCE)
@Target
(
	{
		//ElementType.FIELD,
		//ElementType.PARAMETER,
		//ElementType.METHOD,
		//ElementType.CONSTRUCTOR,
		//ElementType.LOCAL_VARIABLE,

		//ElementType.TYPE_PARAMETER,
		ElementType.TYPE_USE
	}
)
public @interface fixed{}
