package ru.kami.minesweeper.view;

import lombok.extern.slf4j.Slf4j;
import ru.kami.minesweeper.api.Result;
import ru.kami.minesweeper.controller.Controller;
import ru.kami.minesweeper.view.adapter.CellMouseAdapter;
import ru.kami.minesweeper.view.adapter.MenuMouseAdapter;
import ru.kami.minesweeper.view.entity.*;
import ru.kami.minesweeper.view.entity.*;
import ru.kami.minesweeper.view.icon.MinesweeperImageIconRegistry;
import ru.kami.minesweeper.view.constant.GlobalConstants;
import ru.kami.minesweeper.view.constant.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.awt.GridBagConstraints.WEST;
import static ru.kami.minesweeper.view.constant.GameGridPanelConstants.*;

@Slf4j
public class SwingViewGame extends JFrame implements GameView {
    private final CellView[][] cellViews;
    private final Controller controller;
    private final int rowNumber;
    private final int columnNumber;
    private final JPanel container;
    private final UiJLabel minCounter;
    private final UiJLabel timer;
    private final AboutDialog aboutDialog;
    private final GameNewDialog gameNewDialog;
    private final RulesDialog rulesDialog;

    public SwingViewGame(Controller controller, int rowNumber, int columnNumber) {
        super(UIConstants.APP_NAME);
        this.cellViews = new CellView[rowNumber][columnNumber];
        this.controller = controller;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
        this.container = new JPanel(new GridBagLayout());
        this.minCounter = new UiJLabel();
        this.timer = new UiJLabel();
        this.aboutDialog = new AboutDialog(this);
        this.gameNewDialog = new GameNewDialog(this, controller);
        this.rulesDialog = new RulesDialog(this);

        packInFrameContainer();
    }

    private void packInFrameContainer() {
        log.info("Frame rendering started ..");
        JPanel gameGridPanel = renderGameGridPanel();
        UiIconJLabel timerUiIconJLabel = new UiIconJLabel(UIConstants.TIMER_ICON_CODE);
        UiIconJLabel minUiIconJLabel = new UiIconJLabel(UIConstants.MIN_ICON_CODE);
        GameRestartJButton gameRestartJButton = new GameRestartJButton(this, controller);

        container.add(gameGridPanel, getContainerGridBagConstrains(UIConstants.GAME_GRID_X, UIConstants.GAME_GRID_Y, UIConstants.GAME_GRID_WIDTH, UIConstants.GAME_GRID_HEIGHT));
        container.add(minUiIconJLabel, getContainerGridBagConstrains(UIConstants.MIN_ICON_GRID_X, UIConstants.MIN_ICON_GRID_Y, UIConstants.MIN_ICON_GRID_WIDTH, UIConstants.MIN_ICON_GRID_HEIGHT));
        container.add(minCounter, getContainerGridBagConstrains(UIConstants.MIN_COUNTER_GRID_X, UIConstants.MIN_COUNTER_GRID_Y, UIConstants.MIN_COUNTER_GRID_WIDTH, UIConstants.MIN_COUNTER_GRID_HEIGHT));
        container.add(timerUiIconJLabel, getContainerGridBagConstrains(UIConstants.TIMER_ICON_GRID_X, UIConstants.TIMER_ICON_GRID_Y, UIConstants.TIMER_ICON_GRID_WIDTH, UIConstants.TIMER_ICON_GRID_HEIGHT));
        container.add(timer, getContainerGridBagConstrains(UIConstants.TIMER_GRID_X, UIConstants.TIMER_GRID_Y, UIConstants.TIMER_GRID_WIDTH, UIConstants.TIMER_GRID_HEIGHT));
        container.add(gameRestartJButton, getContainerGridBagConstrains(UIConstants.BUTTON_GRID_X, UIConstants.BUTTON_GRID_Y, UIConstants.BUTTON_GRID_WIDTH, UIConstants.BUTTON_GRID_HEIGHT));

        renderFrame();
        renderMenuBar();
        container.setBackground(GlobalConstants.BACKGROUND_COLOR);
        add(container);

        pack();
        log.info("Frame rendering has ended ..");
    }

    private GridBagConstraints getContainerGridBagConstrains(int gridX, int gridY, int gridWidth, int gridHeight) {
        return new GridBagConstraints(gridX, gridY, gridWidth, gridHeight,
                UIConstants.WEIGHT_X, UIConstants.WEIGHT_Y, WEST, WEST,
                new Insets(UIConstants.INSETS_TOP, UIConstants.INSETS_LEFT, UIConstants.INSETS_BOTTOM, UIConstants.INSETS_RIGHT),
                UIConstants.IPAD_X, UIConstants.IPAD_Y);
    }

    private JPanel renderGameGridPanel() {
        JPanel panelMinefield = new JPanel(new GridLayout(rowNumber, columnNumber, GAP_WIDTH, GAP_HEIGHT));
        panelMinefield.setBackground(GlobalConstants.BACKGROUND_COLOR);

        for (int i = 0; i < rowNumber; i++) {
            for (int j = 0; j < columnNumber; j++) {
                CellView cellView = new CellView(i, j);
                cellView.setOpaque(true);
                cellView.setPreferredSize(new Dimension(ACTIVE_CELL_WIDTH, ACTIVE_CELL_HEIGHT));

                Optional<ImageIcon> imageIconOptional = MinesweeperImageIconRegistry.getCellIconMap(INITIAL_CELL_IMAGE_CODE);
                if (imageIconOptional.isPresent()) {
                    cellView.setIcon(imageIconOptional.get());
                } else {
                    log.error("Ошибка в получении иконки c кодом: {}", INITIAL_CELL_IMAGE_CODE);
                }

                CellMouseAdapter cellMouseAdapter = new CellMouseAdapter(controller, cellView);
                List<MouseAdapter> mouseAdapterList = cellMouseAdapter.getCellMouseAdapterList();
                mouseAdapterList.forEach(cellView::addMouseListener);

                panelMinefield.add(cellView);
                cellViews[i][j] = cellView;
            }
        }

        JPanel minefieldWrapper = new JPanel();
        int minefieldWrapperWidth = rowNumber * CELL_HEIGHT;
        int minefieldWrapperHeight = columnNumber * CELL_WIDTH;
        minefieldWrapper.setSize(minefieldWrapperWidth, minefieldWrapperHeight);
        minefieldWrapper.add(panelMinefield);
        minefieldWrapper.setBackground(BORDER_COLOR);
        add(minefieldWrapper);
        return minefieldWrapper;
    }

    private void renderFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(UIConstants.LOCATION_X, UIConstants.LOCATION_Y);
        Optional<Image> imageOptional = MinesweeperImageIconRegistry.getGameImage();
        if (imageOptional.isPresent()) {
            setIconImage(imageOptional.get());
        } else {
            log.error("Ошибка в получении gameImage");
        }
        setVisible(true);
    }

    private void renderMenuBar() {
        JMenuBar jMenuBar = new JMenuBar();
        setJMenuBar(jMenuBar);

        JMenu gameMenu = new JMenu("Game");
        jMenuBar.add(gameMenu);

        List<JMenuItem> menuItemList = new ArrayList<>();
        menuItemList.add(new JMenuItem("New Game"));
        menuItemList.add(new JMenuItem("High Scores"));
        menuItemList.add(new JMenuItem("Exit"));
        menuItemList.forEach(item -> {
            setMouseAdapterToJMenu(item, item.getText());
            gameMenu.add(item);
        });

        JMenu rulesMenu = new JMenu("Rules");
        JMenu aboutMenu = new JMenu("About");
        setMouseAdapterToJMenu(rulesMenu, aboutMenu.getText());
        setMouseAdapterToJMenu(aboutMenu, aboutMenu.getText());
        jMenuBar.add(rulesMenu);
        jMenuBar.add(aboutMenu);
    }

    private void setMouseAdapterToJMenu(JMenuItem jMenuItem, String code) {
        MenuMouseAdapter menuMouseAdapter = new MenuMouseAdapter(controller, this);
        Optional<MouseAdapter> mouseAdapterOptional = menuMouseAdapter.getMenuMouseAdapterMap(jMenuItem.getText());

        if (mouseAdapterOptional.isPresent()) {
            jMenuItem.addMouseListener(mouseAdapterOptional.get());
        } else {
            log.error("Ошибка в получении menuMouseAdapter c кодом: {}", code);
        }
    }

    public void renderGameNewDialog() {
        gameNewDialog.setVisible(true);
    }

    public void renderAboutMenuDialog() {
        aboutDialog.setVisible(true);
    }

    public void renderRulesMenuDialog() {
        rulesDialog.setVisible(true);
    }

    @Override
    public void updateMinLeftStatus(int status) {
        minCounter.setText(String.valueOf(status));
    }

    @Override
    public void updateTimerStatus(int status) {
        timer.setText(String.valueOf(status));
    }

    @Override
    public void renderLoss() {
        new GameEndLoseDialog(this);
    }

    @Override
    public void renderVictory(int successRate) {
        new GameEndVictoryDialog(this, successRate);
    }

    @Override
    public void renderVictoryWithNewRecord(int successRate) {
        new GameEndVictoryWithNewRecordDialog(this, controller, successRate);
    }

    @Override
    public void updateCell(int row, int column, String code) {
        log.info("Cell update row: {} column: {} с кодом {}", row, column, code);
        if (MinesweeperImageIconRegistry.getCellIconMap(code).isPresent()) {
            cellViews[row][column].setIcon(MinesweeperImageIconRegistry.getCellIconMap(code).get());
        } else {
            log.error("Ошибка в получении иконки c кодом: {}", code);
        }
    }

    @Override
    public void renderHighScore(Result[] results) {
        new HighScoreTableDialog(this, ResultView.valueOf(results));
    }

    public void requestHighScore() {
        controller.handleUserClickedOnHighScore();
    }
}
