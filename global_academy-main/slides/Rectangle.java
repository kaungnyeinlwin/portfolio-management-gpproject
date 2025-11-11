public class Rectangle extends ClosedShape
{  private double myWidth;
   private double myHeight;

   public Rectangle()
   {  init(0, 0);
   }

   public Rectangle(double width, double height)
   {  init(width, height);
   }

   public Rectangle(double x, double y,
		double width, double height)
   {  super(x, y);
      init(width, height);
   }

   private void init(double width, double height)
   {  myWidth = width;
      myHeight = height;
   }
}
