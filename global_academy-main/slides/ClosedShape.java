public class ClosedShape 
{  private double myX;
   private double myY;
	
   public ClosedShape()
   {  this(0,0);	}

   public ClosedShape (double x, double y)
   {  myX = x;   
      myY = y;  
   }

   public String toString()
   { return "x: " + getX() + " y: " + getY();	}

   public double getX(){ return myX; }
   public double getY(){ return myY; }
}
