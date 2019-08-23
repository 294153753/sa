import com.vividsolutions.jts.geom.Coordinate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Display extends JPanel {

    Coordinate[] area1;
    Coordinate[] area2;
    Coordinate[] area3;

    private Display(Coordinate[] area1, Coordinate[] area2, Coordinate[] area3) {
        this.area1 = area1;
        this.area2 = area2;
        this.area3 = area3;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Polygon a1 = new Polygon();
        Polygon a2 = new Polygon();
        Polygon a3 = new Polygon();
        for (int i = 0; i < area1.length; i++){
            a1.addPoint((int) (area1[i].x * 80 + 20), (int) (1000 - area1[i].y * 80));
        }
        for (int i = 0; i < area2.length; i++){
            a2.addPoint((int) (area2[i].x * 80 + 20), (int) (1000 - area2[i].y * 80));
        }
        for (int i = 0; i < area3.length; i++){
            a3.addPoint((int) (area3[i].x * 80 + 20), (int) (1000 - area3[i].y * 80));
        }
        g.drawPolygon(a1);
        g.drawPolygon(a2);
        g.drawPolygon(a3);
    }

    public static void draw(Coordinate[] room, Coordinate[] area1, Coordinate[] area2) {
        JFrame frame = new JFrame();
        frame.setTitle("Test");
        frame.setSize(1500, 1000);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        Container contentPane = frame.getContentPane();
        contentPane.add(new Display(room, area1, area2));
        frame.setVisible(true);
    }
}
