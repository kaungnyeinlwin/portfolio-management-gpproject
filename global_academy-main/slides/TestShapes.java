public class TestShapes {
    public static void main(String[] args) {
 	Rectangle r = new Rectangle(1, 2, 3, 4);
        ClosedShape s = new ClosedShape(2, 3);
        System.out.println( s.getX() );
        System.out.println( s.getY() );
	System.out.println( s.toString() ); 
	System.out.println( r.getX() );
	System.out.println( r.getY() );
	System.out.println( r.toString() ); 
    }
}
