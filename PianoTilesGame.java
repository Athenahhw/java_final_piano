import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class PianoTilesGame extends JPanel implements ActionListener, MouseListener, KeyListener {
    // 遊戲設定
    private final int COLUMN_COUNT = 4;
    private final int TILE_WIDTH = 100;
    private final int TILE_HEIGHT = 150;
    private final int SCORE_LINE_POSITION = 540;
    private Timer timer;
    private ArrayList<Integer> tiles;
    private int scrollY = 0;
    private boolean isGameOver = false;
    private int score = 0;
    private int speed = 5;
    private Random random;
    private boolean[] keyPressed = new boolean[4];
    private ArrayList<Boolean> tilesHit;
    
    // 遊戲狀態
    private enum GameState {
        MENU,       // 開始選單
        COUNTDOWN,  // 倒數階段
        PLAYING,    // 遊戲進行中
        GAME_OVER   // 遊戲結束
    }
    private GameState gameState = GameState.MENU;
    private int countdownSeconds = 3;

    public PianoTilesGame() {
        this.setPreferredSize(new Dimension(COLUMN_COUNT * TILE_WIDTH, 600));
        this.setBackground(Color.WHITE);
        this.addMouseListener(this);
        this.addKeyListener(this);
        this.setFocusable(true);
        
        random = new Random();
        tiles = new ArrayList<>();
        tilesHit = new ArrayList<>();
        
        // 啟動遊戲迴圈
        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 根據遊戲狀態顯示不同畫面
        if (gameState == GameState.MENU) {
            drawMenuScreen(g, g2d);
            return;
        } else if (gameState == GameState.COUNTDOWN) {
            drawCountdownScreen(g, g2d);
            return;
        }
        //jjj
        // 遊戲進行中或結束時的畫面
        // 繪製白色格子背景
        g.setColor(new Color(245, 245, 245));
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < COLUMN_COUNT; col++) {
                int y = row * TILE_HEIGHT;
                g.fillRect(col * TILE_WIDTH, y, TILE_WIDTH, TILE_HEIGHT);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(col * TILE_WIDTH, y, TILE_WIDTH, TILE_HEIGHT);
                g.setColor(new Color(245, 245, 245));
            }
        }
        
        // 繪製黑色方塊
        for (int i = 0; i < tiles.size(); i++) {
            int blackIndex = tiles.get(i);
            int y = (i * TILE_HEIGHT) - scrollY;
            boolean isHit = tilesHit.get(i);
            
            if (y + TILE_HEIGHT > 0 && y < 600) {
                if (isHit) {
                    // 被擊中的方塊 - 彩色漸變
                    Color hitColor;
                    switch (blackIndex) {
                        case 0:
                            hitColor = new Color(255, 100, 100);
                            break;
                        case 1:
                            hitColor = new Color(100, 255, 100);
                            break;
                        case 2:
                            hitColor = new Color(100, 200, 255);
                            break;
                        case 3:
                            hitColor = new Color(255, 200, 100);
                            break;
                        default:
                            hitColor = Color.GRAY;
                    }
                    GradientPaint gradient = new GradientPaint(
                        blackIndex * TILE_WIDTH, y, hitColor,
                        blackIndex * TILE_WIDTH, y + TILE_HEIGHT, hitColor.darker()
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(blackIndex * TILE_WIDTH, y, TILE_WIDTH, TILE_HEIGHT);
                    
                    // 畫「✓」符號
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 40));
                    String checkMark = "✓";
                    int textWidth = g.getFontMetrics().stringWidth(checkMark);
                    g.drawString(checkMark, 
                        blackIndex * TILE_WIDTH + (TILE_WIDTH - textWidth) / 2, 
                        y + TILE_HEIGHT / 2 + 15);
                } else {
                    // 未擊中的方塊 - 黑色漸變
                    GradientPaint gradient = new GradientPaint(
                        blackIndex * TILE_WIDTH, y, new Color(50, 50, 50),
                        blackIndex * TILE_WIDTH, y + TILE_HEIGHT, Color.BLACK
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(blackIndex * TILE_WIDTH, y, TILE_WIDTH, TILE_HEIGHT);
                }
                
                g.setColor(Color.GRAY);
                g.drawRect(blackIndex * TILE_WIDTH, y, TILE_WIDTH, TILE_HEIGHT);
            }
        }
        
        // 繪製計分線
        g2d.setColor(new Color(255, 0, 0, 200));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10}, 0));
        g2d.drawLine(0, SCORE_LINE_POSITION, getWidth(), SCORE_LINE_POSITION);
        
        // 繪製底部按鈕區域
        g.setFont(new Font("Arial", Font.BOLD, 20));
        for (int col = 0; col < COLUMN_COUNT; col++) {
            if (keyPressed[col]) {
                g.setColor(new Color(100, 200, 255, 150));
                g.fillRect(col * TILE_WIDTH, SCORE_LINE_POSITION, TILE_WIDTH, 600 - SCORE_LINE_POSITION);
            }
            
            g.setColor(Color.DARK_GRAY);
            String keyText = String.valueOf(col + 1);
            int textWidth = g.getFontMetrics().stringWidth(keyText);
            g.drawString(keyText, col * TILE_WIDTH + (TILE_WIDTH - textWidth) / 2, 580);
        }
        
        // 顯示分數
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("分數: " + score, 10, 30);
        
        // 顯示速度
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("速度: " + speed, 10, 55);
        
        // Game Over 畫面
        if (gameState == GameState.GAME_OVER) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String gameOverText = "遊戲結束!";
            int textWidth = g.getFontMetrics().stringWidth(gameOverText);
            g.drawString(gameOverText, (getWidth() - textWidth) / 2, 250);
            
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            String scoreText = "最終分數: " + score;
            textWidth = g.getFontMetrics().stringWidth(scoreText);
            g.drawString(scoreText, (getWidth() - textWidth) / 2, 300);
            
            String restartText = "按空白鍵或 Enter 重新開始";
            textWidth = g.getFontMetrics().stringWidth(restartText);
            g.drawString(restartText, (getWidth() - textWidth) / 2, 350);
        }
    }
    
    // 繪製開始選單
    private void drawMenuScreen(Graphics g, Graphics2D g2d) {
        // 繪製背景漸變
        GradientPaint bgGradient = new GradientPaint(
            0, 0, new Color(30, 30, 50),
            0, 600, new Color(10, 10, 30)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // 繪製標題
        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.BOLD, 56));
        String title = "別踩白塊兒";
        int textWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (getWidth() - textWidth) / 2, 120);
        
        // 繪製副標題
        g.setFont(new Font("Arial", Font.ITALIC, 24));
        g.setColor(new Color(150, 200, 255));
        String subtitle = "Piano Tiles Game";
        textWidth = g.getFontMetrics().stringWidth(subtitle);
        g.drawString(subtitle, (getWidth() - textWidth) / 2, 160);
        
        // 繪製裝飾性鋼琴鍵
        int keyY = 200;
        for (int i = 0; i < 4; i++) {
            int x = i * 100;
            g.setColor(i % 2 == 0 ? Color.BLACK : Color.WHITE);
            g.fillRect(x, keyY, 100, 40);
            g.setColor(Color.GRAY);
            g.drawRect(x, keyY, 100, 40);
        }
        
        // 繪製開始按鈕
        int buttonWidth = 220;
        int buttonHeight = 70;
        int buttonX = (getWidth() - buttonWidth) / 2;
        int buttonY = 300;
        
        // 按鈕背景（漸變）
        GradientPaint buttonGradient = new GradientPaint(
            buttonX, buttonY, new Color(80, 200, 120),
            buttonX, buttonY + buttonHeight, new Color(50, 170, 90)
        );
        g2d.setPaint(buttonGradient);
        g2d.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 25, 25);
        
        // 按鈕邊框
        g2d.setColor(new Color(60, 150, 80));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 25, 25);
        
        // 按鈕文字
        g.setColor(Color.WHITE);
        g.setFont(new Font("微軟正黑體", Font.BOLD, 32));
        String buttonText = "開始遊戲";
        textWidth = g.getFontMetrics().stringWidth(buttonText);
        g.drawString(buttonText, (getWidth() - textWidth) / 2, buttonY + 47);
        
        // 繪製操作說明
        g.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
        g.setColor(new Color(180, 180, 200));
        String[] instructions = {
            "▶ 操作說明 ◀",
            "",
            "按下鍵盤 1、2、3、4 控制四個欄位",
            "當黑色方塊接近紅線時按下對應按鍵",
            "",
            "完美擊中 (接近紅線中心) → 得 2 分",
            "普通擊中 (穿過紅線範圍) → 得 1 分",
            "漏掉方塊 → 遊戲結束"
        };
        
        int instructY = 420;
        for (String instruction : instructions) {
            textWidth = g.getFontMetrics().stringWidth(instruction);
            g.drawString(instruction, (getWidth() - textWidth) / 2, instructY);
            instructY += 22;
        }
        
        // 提示文字（閃爍效果）
        long currentTime = System.currentTimeMillis();
        if ((currentTime / 500) % 2 == 0) {
            g.setFont(new Font("微軟正黑體", Font.PLAIN, 18));
            g.setColor(new Color(255, 255, 100));
            String hint = "點擊按鈕或按空白鍵開始";
            textWidth = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, (getWidth() - textWidth) / 2, 580);
        }
    }
    
    // 繪製倒數畫面
    private void drawCountdownScreen(Graphics g, Graphics2D g2d) {
        // 繪製背景
        GradientPaint bgGradient = new GradientPaint(
            0, 0, new Color(20, 20, 40),
            0, 600, new Color(40, 10, 60)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // 繪製倒數數字
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 150));
        String countText = String.valueOf(countdownSeconds);
        if (countdownSeconds == 0) {
            countText = "GO!";
            g.setFont(new Font("Arial", Font.BOLD, 100));
        }
        int textWidth = g.getFontMetrics().stringWidth(countText);
        g.drawString(countText, (getWidth() - textWidth) / 2, 330);
        
        // 繪製倒數數字的外框（霓虹效果）
        g.setColor(new Color(100, 200, 255, 100));
        g.setFont(new Font("Arial", Font.BOLD, countdownSeconds == 0 ? 105 : 155));
        textWidth = g.getFontMetrics().stringWidth(countText);
        g.drawString(countText, (getWidth() - textWidth) / 2 - 2, 332);
        
        // 繪製提示文字
        g.setFont(new Font("微軟正黑體", Font.PLAIN, 24));
        g.setColor(new Color(200, 200, 200));
        String hint = countdownSeconds > 0 ? "準備好你的手指！" : "開始！";
        textWidth = g.getFontMetrics().stringWidth(hint);
        g.drawString(hint, (getWidth() - textWidth) / 2, 450);
        
        // 繪製鍵盤提示
        if (countdownSeconds <= 1) {
            g.setFont(new Font("Arial", Font.BOLD, 18));
            for (int i = 0; i < 4; i++) {
                g.setColor(new Color(100, 100, 150));
                g.fillRect(i * 100 + 20, 500, 60, 60);
                g.setColor(new Color(150, 150, 200));
                g.drawRect(i * 100 + 20, 500, 60, 60);
                g.setColor(Color.WHITE);
                String key = String.valueOf(i + 1);
                textWidth = g.getFontMetrics().stringWidth(key);
                g.drawString(key, i * 100 + 50 - textWidth / 2, 538);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 倒數階段
        if (gameState == GameState.COUNTDOWN) {
            repaint();
            return;
        }
        
        // 只在遊戲進行中才更新
        if (gameState != GameState.PLAYING) {
            repaint();
            return;
        }
        
        if (isGameOver) return;
        
        // 更新位置
        scrollY += speed;
        
        // 每 10 分加速
        int newSpeed = 5 + (score / 10);
        if (newSpeed != speed) {
            speed = newSpeed;
        }
        
        // 檢查是否需要新增方塊
        if (tiles.size() > 0) {
            int lastTileY = ((tiles.size() - 1) * TILE_HEIGHT) - scrollY;
            if (lastTileY > 0) {
                addNewTile();
            }
        }
        
        // 檢查是否漏接
        if (tiles.size() > 0) {
            int firstTileY = -scrollY;
            int firstTileBottomY = firstTileY + TILE_HEIGHT;
            boolean firstTileHit = tilesHit.get(0);
            
            if (firstTileBottomY > SCORE_LINE_POSITION + 50 && !firstTileHit) {
                isGameOver = true;
                gameState = GameState.GAME_OVER;
                timer.stop();
            }
            
            // 移除已離開畫面的方塊
            while (tiles.size() > 0 && -scrollY + TILE_HEIGHT < -100) {
                tiles.remove(0);
                tilesHit.remove(0);
                scrollY -= TILE_HEIGHT;
            }
        }
        
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // 開始選單：檢查是否點擊開始按鈕
        if (gameState == GameState.MENU) {
            int buttonWidth = 220;
            int buttonHeight = 70;
            int buttonX = (getWidth() - buttonWidth) / 2;
            int buttonY = 300;
            
            int mouseX = e.getX();
            int mouseY = e.getY();
            
            if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                startCountdown();
            }
            return;
        }
        
        // 遊戲結束：點擊重新開始
        if (gameState == GameState.GAME_OVER) {
            restartGame();
            return;
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        // 開始選單：按空白鍵開始
        if (gameState == GameState.MENU) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                startCountdown();
            }
            return;
        }
        
        // 遊戲結束：按空白鍵重新開始
        if (gameState == GameState.GAME_OVER) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                restartGame();
            }
            return;
        }
        
        // 遊戲進行中：1234 控制
        if (gameState != GameState.PLAYING) return;
        
        int col = -1;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1:
                col = 0;
                break;
            case KeyEvent.VK_2:
                col = 1;
                break;
            case KeyEvent.VK_3:
                col = 2;
                break;
            case KeyEvent.VK_4:
                col = 3;
                break;
        }
        
        if (col != -1) {
            keyPressed[col] = true;
            checkTileHit(col);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_1:
                keyPressed[0] = false;
                break;
            case KeyEvent.VK_2:
                keyPressed[1] = false;
                break;
            case KeyEvent.VK_3:
                keyPressed[2] = false;
                break;
            case KeyEvent.VK_4:
                keyPressed[3] = false;
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    private void checkTileHit(int col) {
        int bestIndex = -1;
        int bestDistance = Integer.MAX_VALUE;
        
        for (int i = 0; i < tiles.size(); i++) {
            int tileY = (i * TILE_HEIGHT) - scrollY;
            int tileBottomY = tileY + TILE_HEIGHT;
            int blackCol = tiles.get(i);
            boolean isHit = tilesHit.get(i);
            
            if (blackCol != col || isHit) continue;
            
            if (tileY <= SCORE_LINE_POSITION && tileBottomY >= SCORE_LINE_POSITION) {
                int tileCenterY = tileY + TILE_HEIGHT / 2;
                int distance = Math.abs(tileCenterY - SCORE_LINE_POSITION);
                
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = i;
                }
            }
        }
        
        if (bestIndex != -1) {
            tilesHit.set(bestIndex, true);
            playDrumSound(col);
            
            int perfectRange = 30;
            if (bestDistance <= perfectRange) {
                score += 2;
            } else {
                score += 1;
            }
        }
    }
    
    private void playDrumSound(int col) {
        new Thread(() -> {
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception e) {
            }
        }).start();
    }
    
    private void addNewTile() {
        tiles.add(random.nextInt(COLUMN_COUNT));
        tilesHit.add(false);
    }
    
    // 開始倒數
    private void startCountdown() {
        gameState = GameState.COUNTDOWN;
        countdownSeconds = 3;
        
        Timer countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdownSeconds--;
                
                if (countdownSeconds < 0) {
                    ((Timer)e.getSource()).stop();
                    startGame();
                }
                repaint();
            }
        });
        countdownTimer.start();
    }
    
    // 正式開始遊戲
    private void startGame() {
        gameState = GameState.PLAYING;
        
        tiles.clear();
        tilesHit.clear();
        for(int i = 0; i < 6; i++) {
            tiles.add(random.nextInt(COLUMN_COUNT));
            tilesHit.add(false);
        }
        
        scrollY = 0;
        score = 0;
        speed = 5;
        isGameOver = false;
        
        if (!timer.isRunning()) {
            timer.start();
        }
    }
    
    private void restartGame() {
        gameState = GameState.MENU;
        tiles.clear();
        tilesHit.clear();
        scrollY = 0;
        score = 0;
        speed = 5;
        isGameOver = false;
        repaint();
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("別踩白塊兒 - Piano Tiles");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            PianoTilesGame panel = new PianoTilesGame();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}