import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// --- Base abstract class ---
abstract class UIComponent {
    protected String name;
    protected JComponent swingComponent;

    public UIComponent(String name) {
        this.name = name;
    }

    public abstract JComponent getComponent();
    public abstract void render();
}

// --- Clickable interface ---
interface Clickable {
    void click();
}

// --- Label component ---
class Label extends UIComponent {
    private JLabel label;

    public Label(String name, String text) {
        super(name);
        label = new JLabel(text);
        swingComponent = label;
    }

    @Override
    public JComponent getComponent() {
        return label;
    }

    @Override
    public void render() {
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
    }

    public void setText(String text) {
        label.setText(text);
    }
}

// --- Button component ---
class Button extends UIComponent implements Clickable {
    private JButton button;
    private Runnable onClickAction;

    public Button(String name, String text) {
        super(name);
        button = new JButton(text);
        swingComponent = button;
    }

    @Override
    public JComponent getComponent() {
        return button;
    }

    @Override
    public void render() {
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.addActionListener(e -> click());
    }

    @Override
    public void click() {
        if (onClickAction != null) {
            onClickAction.run();
        } else {
            System.out.println(name + " clicked!");
        }
    }

    public void setOnClick(Runnable action) {
        this.onClickAction = action;
    }
}

// --- The demo application ---
public class FakeGUIDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Fake GUI Polymorphism Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 150);
            frame.setLayout(new FlowLayout());

            // Create components
            Label label = new Label("StatusLabel", "Hello, world!");
            Button button = new Button("ClickMeButton", "Click Me");

            // Polymorphism: store them in an array of the abstract type
            UIComponent[] components = { label, button };

            for (UIComponent c : components) {
                c.render();
                frame.add(c.getComponent());
            }

            // Connect behavior (using the Clickable interface)
            button.setOnClick(() -> label.setText("Button was clicked!"));

            frame.setVisible(true);
        });
    }
}

