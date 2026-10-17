package lcdtest;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/** A simple scrolling text viewer with proportional-font wrapping. */
public final class TextCanvas extends Canvas implements CommandListener {

    private final Display display;
    private final Displayable back;
    private final String heading;
    private final String text;
    private final Command backCommand = new Command("Back", Command.BACK, 1);
    private final Command topCommand = new Command("Top", Command.SCREEN, 2);
    private final Command bottomCommand = new Command("End", Command.SCREEN, 3);

    private String[] lines;
    private int layoutWidth = -1;
    private int top;

    public TextCanvas(Display display, Displayable back, String heading, String text) {
        this.display = display;
        this.back = back;
        this.heading = heading;
        this.text = text == null ? "" : text;
        setTitle(heading);
        addCommand(backCommand);
        addCommand(topCommand);
        addCommand(bottomCommand);
        setCommandListener(this);
    }

    private void layout(int width) {
        if (width == layoutWidth && lines != null) {
            return;
        }
        layoutWidth = width;
        lines = Util.wrap(text, Font.getDefaultFont(), Math.max(16, width - 4));
    }

    private int rows() {
        Font f = Font.getDefaultFont();
        int avail = getHeight() - (f.getHeight() + 6) - (2 * f.getHeight() + 6);
        return Math.max(1, avail / f.getHeight());
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        Font f = Font.getDefaultFont();
        int titleH = f.getHeight() + 4;
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, w, h);
        g.setColor(0x000000);
        g.fillRect(0, 0, w, titleH);
        g.setColor(0xFFFFFF);
        g.drawString(heading, 2, 2, Graphics.TOP | Graphics.LEFT);

        layout(w);
        int lh = f.getHeight();
        int visible = rows();
        if (top > Math.max(0, lines.length - visible)) {
            top = Math.max(0, lines.length - visible);
        }
        g.setColor(0x000000);
        int y = titleH + 2;
        for (int i = 0; i < visible && top + i < lines.length; i++) {
            g.drawString(lines[top + i], 2, y, Graphics.TOP | Graphics.LEFT);
            y += lh;
        }
        int footerY = h - (2 * lh + 4);
        g.setColor(0xE0E0E0);
        g.fillRect(0, footerY, w, 2 * lh + 4);
        g.setColor(0x000000);
        g.drawString("UP/DOWN scroll   LEFT/BACK return", 2, footerY + 1,
                Graphics.TOP | Graphics.LEFT);
        g.drawString((top + 1) + "-" + Math.min(lines.length, top + visible)
                + " of " + lines.length, 2, footerY + lh + 2,
                Graphics.TOP | Graphics.LEFT);
        // Scrollbar
        if (lines.length > visible) {
            int barH = Math.max(6, (h - titleH - 2 * lh) * visible / lines.length);
            int barY = titleH + (h - titleH - 2 * lh - barH) * top
                    / Math.max(1, lines.length - visible);
            g.setColor(0x808080);
            g.fillRect(w - 3, barY, 2, barH);
        }
    }

    protected void keyPressed(int keyCode) {
        int action = 0;
        try {
            action = getGameAction(keyCode);
        } catch (Throwable t) {
            // unknown key: fall back to numeric mappings below
        }
        if (action == Canvas.UP || keyCode == Canvas.KEY_NUM2) {
            scroll(-1);
        } else if (action == Canvas.DOWN || keyCode == Canvas.KEY_NUM8) {
            scroll(1);
        } else if (action == Canvas.FIRE || keyCode == Canvas.KEY_NUM5) {
            scroll(rows() - 1);
        } else if (action == Canvas.LEFT || action == Canvas.RIGHT) {
            back();
        }
    }

    protected void pointerPressed(int x, int y) {
        Font f = Font.getDefaultFont();
        if (y > getHeight() - (2 * f.getHeight() + 6)) {
            return;
        }
        scroll(y < getHeight() / 2 ? -1 : 1);
    }

    private void scroll(int delta) {
        layout(getWidth());
        top += delta;
        if (top < 0) {
            top = 0;
        }
        int max = Math.max(0, lines.length - rows());
        if (top > max) {
            top = max;
        }
        repaint();
    }

    private void back() {
        display.setCurrent(back);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            back();
        } else if (c == topCommand) {
            top = 0;
            repaint();
        } else if (c == bottomCommand) {
            layout(getWidth());
            top = Math.max(0, lines.length - rows());
            repaint();
        }
    }
}
