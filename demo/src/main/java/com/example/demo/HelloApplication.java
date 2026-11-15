package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HelloApplication extends Application {
    // Dimensions sécurisées pour garder la barre Windows (X, Agrandir, Réduire) toujours visible
    public static final double SCREEN_WIDTH = 1150, SCREEN_HEIGHT = 700;
    public static final int NBcases = 20;
    public static int LENcases = 25;
    public static final int[] ORIGIN = {360, 140};

    // Difficulté nettement augmentée : propagation rapide dès le Niveau 1 et forte accélération à chaque niveau !
    public static final double BASE_SPREAD_RATE = 1.15;
    public static final double WAVE_SPEED_INCREMENT = 0.42;

    // Coûts tactiques équilibrés pour plus de challenge
    public static final double FIREWALL_COST = 12.0;
    public static final double REPAIR_COST = 8.0;
    public static final double PURGE_COST = 18.0;
    public static final double EMP_COST = 45.0;
    public static final double TURRET_COST = 40.0;
    public static final double MINE_COST = 28.0;
    public static final double HONEYPOT_COST = 28.0;

    public static final double HYPER_SURGE_COOLDOWN_MAX = 18.0;

    // Formes et styles variés pour les Pop-ups (placés hors de la grille !)
    public enum PopupStyle {
        NEON_FLOAT,
        STARBURST,
        HEX_TECH,
        SPEECH_BUBBLE,
        WARNING_SHIELD
    }

    public Pane pane = new Pane();
    public Scene scene = new Scene(pane, SCREEN_WIDTH, SCREEN_HEIGHT);
    public Canvas bgCanvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
    public GraphicsContext bgContext = bgCanvas.getGraphicsContext2D();
    public Random random = new Random();

    // Synthétiseur Audio Techno 126 BPM & Bruitages
    public CyberAudioEngine audio = new CyberAudioEngine();

    public double time = 0;
    public double spreadAccumulator = 0;

    // MODE DE JEU : false = BLUE TEAM (Défenseur SOC), true = RED TEAM (Infiltration Virus)
    public boolean redTeamMode = false;

    // Choix de Classe (Cyber-Deck) : 0 = Architecte, 1 = Netrunner, 2 = Technomancien
    public int selectedDeck = 1;

    // ALTERNANCE DE STYLE : false = Futuriste (Niveaux 1, 3, 5...), true = Bande Dessinée (Niveaux 2, 4, 6...)
    public boolean comicMode = false;

    // Statistiques évolutives
    public double maxBandwidth = 150.0;
    public double bandwidth = 150.0;
    public double bandwidthRegenPerSec = 36.0;
    public double hyperSurgeDuration = 6.0;
    public double turretFireInterval = 1.15;
    public double nukeCost = 48.0;
    public int perimeterShields = 1;

    // IA Défenseur SOC (Ennemi du joueur en mode RED TEAM)
    public double aiDefenseTimer = 0.55;

    // SYSTÈME DE ROLLBACK TEMPOREL [R]
    public int rollbackCharges = 2;
    public double snapshotTimer = 0;
    public double rewindEffectTimer = 0;
    public LinkedList<GridSnapshot> snapshotHistory = new LinkedList<>();

    // Niveaux d'amélioration de la Boutique
    public int upgBwLevel = 1;
    public int upgRegenLevel = 1;
    public int upgSurgeLevel = 1;
    public int droneCount = 0;

    // Score, Crédits, Rangs & Médailles
    public int score = 0;
    public int credits = 250;
    public int killCount = 0;
    public int waveKills = 0;
    public int missilesIntercepted = 0;
    public int totalMedalsEarned = 0;
    public int wave = 1;
    public int combo = 0;
    public double comboTimer = 0;
    public double doubleXpTimer = 0;
    public double empFreezeTimer = 0;
    public int currentRankIndex = 0;

    public String activeMedalBanner = "";
    public double medalBannerTimer = 0;

    // Mode Hyper-Surge
    public double hyperSurgeCharge = 0;
    public double hyperSurgeTimer = 0;

    // BOUTON ROUGE : Rayon Orbital (Blue Team) OU Onde Zero-Day (Red Team)
    public double orbitalCharge = 50.0;
    public boolean orbitalBeamActive = false;
    public double orbitalBeamY = 0;
    public int lastOrbitalRowSwept = -1;

    // Météo & Systèmes
    public double blackoutTimer = 0;
    public double nextWeatherEventTimer = 18.0;
    public double airdropSpawnTimer = 9.0;
    public double teslaZapTimer = 0;
    public double antivirusTickTimer = 0;
    public double missileSpawnTimer = 2.8;

    public int selectedTool = 1;
    public int popupSideToggle = 0; // Alterne gauche/droite pour les pop-ups hors grille

    // Portails Quantiques A et B
    public boolean portalsUnlocked = false;
    public int portalAX = 2, portalAY = 10;
    public int portalBX = 17, portalBY = 10;

    // Effets caméra
    public double shakeIntensity = 0;
    public double screenFlashAlpha = 0;
    public Color screenFlashColor = Color.web("#05D5FA");
    public double audioEnergy = 0;

    public boolean roundEnding = false;
    public double victoryDelayTimer = 0;

    public double mouseX = 0, mouseY = 0;
    public int hoverX = -1, hoverY = -1;
    public Cell[][] grid = new Cell[NBcases][NBcases];
    public Plan currentPlan = Plan.ACCUEIL;

    // Entités visuelles
    public List<Particle> particles = new ArrayList<>();
    public List<FloatingText> floatingTexts = new ArrayList<>();
    public List<Shockwave> shockwaves = new ArrayList<>();
    public List<CyberLaser> lasers = new ArrayList<>();
    public List<VirusMissile> virusMissiles = new ArrayList<>();
    public List<SwarmProjectile> swarmProjectiles = new ArrayList<>();
    public List<AllyDrone> drones = new ArrayList<>();
    public List<SupplyDrop> supplyDrops = new ArrayList<>();
    public List<int[]> activeTeslaLinks = new ArrayList<>();

    public static final String[] RANK_NAMES = {
            "SENTINELLE BRONZE",
            "DÉFENSEUR ARGENT",
            "SPECTRE D'OR",
            "ARCHONTE DIAMANT",
            "CYBER-LÉGENDE OMÉGA"
    };
    public static final int[] RANK_THRESHOLDS = {0, 2500, 6500, 13000, 22000};
    public static final String[] RANK_COLORS = {"#CD7F32", "#E2E8F0", "#FACC15", "#38BDF8", "#FF2A6D"};

    public static final String[] COMIC_WORDS = {"BAM!", "ZAP!", "KAPOW!", "BOUM!", "BZZZT!", "CRACK!", "POW!", "WHAM!"};
    public static final String[] FUTURISTIC_WORDS = {"PURGE!", "CRITICAL!", "SYNTH_HIT!", "DELETED!", "OVERCLOCK!", "CLEANSED!"};

    @Override
    public void start(Stage stage) throws IOException {
        bgCanvas.widthProperty().bind(scene.widthProperty());
        bgCanvas.heightProperty().bind(scene.heightProperty());

        audio.startEngine();
        setupControls(stage);
        updateLayoutMetrics();
        loadNewScreen(currentPlan);

        AnimationTimer timer = new AnimationTimer() {
            long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double dt = Math.min(0.05, (now - lastTime) * 1e-9);
                lastTime = now;

                updateLayoutMetrics();

                if (currentPlan == Plan.GAMEPLAY) {
                    time += dt;

                    if (redTeamMode) {
                        int myVirusCells = countTotalMalwareCells();
                        double effectiveRegen = bandwidthRegenPerSec + (myVirusCells * 1.5);
                        bandwidth = Math.min(maxBandwidth, bandwidth + effectiveRegen * dt);
                    } else {
                        int ransomwareCount = countCells(CellState.RANSOMWARE);
                        int activeDataCores = countCells(CellState.DATA_CORE);
                        double effectiveRegen = Math.max(18.0, bandwidthRegenPerSec + (activeDataCores * 4.5) - (ransomwareCount * 4.0));
                        bandwidth = Math.min(maxBandwidth, bandwidth + effectiveRegen * dt);
                    }

                    snapshotTimer += dt;
                    if (snapshotTimer >= 1.0 && !roundEnding) {
                        snapshotTimer = 0;
                        recordGridSnapshot();
                    }
                    if (rewindEffectTimer > 0) {
                        rewindEffectTimer = Math.max(0, rewindEffectTimer - dt);
                    }

                    updateHyperSurge(dt);
                    updateOrbitalSweep(dt);
                    updateWeatherAndAirdrops(dt);
                    updateAntivirusReplication(dt);

                    if (comboTimer > 0) {
                        comboTimer -= dt;
                        if (comboTimer <= 0) combo = 0;
                    }
                    if (doubleXpTimer > 0) doubleXpTimer = Math.max(0, doubleXpTimer - dt);
                    if (empFreezeTimer > 0) empFreezeTimer = Math.max(0, empFreezeTimer - dt);
                    if (blackoutTimer > 0) blackoutTimer = Math.max(0, blackoutTimer - dt);
                    if (medalBannerTimer > 0) medalBannerTimer = Math.max(0, medalBannerTimer - dt);
                    if (shakeIntensity > 0) shakeIntensity = Math.max(0, shakeIntensity - dt * 28.0);
                    if (screenFlashAlpha > 0) screenFlashAlpha = Math.max(0, screenFlashAlpha - dt * 2.5);
                    if (audioEnergy > 0) audioEnergy = Math.max(0, audioEnergy - dt * 3.2);

                    updateGridTurretsTeslaAndMines(dt);
                    updateDrones(dt);
                    updateMissilesAndSwarms(dt);
                    updateVisualEffects(dt);

                    if (roundEnding) {
                        victoryDelayTimer -= dt;
                        if (victoryDelayTimer <= 0) {
                            roundEnding = false;
                            wave++;
                            rollbackCharges++;
                            if (wave >= 4 && droneCount < wave - 2) {
                                droneCount++;
                            }
                            loadNewScreen(Plan.SHOP);
                            return;
                        }
                    } else if (redTeamMode) {
                        updateRedTeamGameplayLoop(dt);
                    } else {
                        if (empFreezeTimer <= 0) {
                            // Les Missiles commencent dès le Niveau 2 et sont plus fréquents
                            if (wave >= 2) {
                                missileSpawnTimer -= dt;
                                if (missileSpawnTimer <= 0) {
                                    launchEnemyVirusMissile();
                                    missileSpawnTimer = Math.max(0.95, 3.4 - wave * 0.35);
                                }
                            }

                            spreadAccumulator += dt * getCurrentSpreadRate();
                            if (spreadAccumulator >= 1.0) {
                                spreadAccumulator -= 1.0;
                                spreadMalware();
                                checkFloodFillEncirclement();

                                if (checkPerimeterBreach()) {
                                    audio.playGameOver();
                                    triggerScreenShake(22.0, Color.web("#FF2A6D"), 0.6);
                                    loadNewScreen(Plan.GAME_OVER);
                                    return;
                                } else if (isAllMalwareContainedOrPurged()) {
                                    triggerContainmentVictory();
                                }
                            }
                        }
                    }

                    renderGame();
                }
            }
        };
        timer.start();

        stage.setTitle("CYBER_CONTAINMENT // [BLUE TEAM: Défense SOC | RED TEAM: Infiltration Hacker]");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnCloseRequest(e -> audio.stopEngine());
        stage.show();
    }

    // =========================================================================
    // LOGIQUE DU MODE RED TEAM : IA DÉFENSEUR SOC vs JOUEUR VIRUS
    // =========================================================================

    private void updateRedTeamGameplayLoop(double dt) {
        spreadAccumulator += dt * (0.85 + (selectedDeck == 1 ? 0.20 : 0));
        if (spreadAccumulator >= 1.0) {
            spreadAccumulator -= 1.0;
            spreadMalware();

            if (hasRedTeamEscapedOrHackedAllCores()) {
                triggerRedTeamVictory();
                return;
            }
        }

        if (empFreezeTimer <= 0) {
            aiDefenseTimer -= dt;
            double aiInterval = Math.max(0.24, 0.68 - wave * 0.08);
            if (aiDefenseTimer <= 0) {
                aiDefenseTimer = aiInterval;
                performAiSocDefenseAction();

                if (countTotalMalwareCells() == 0 || isAllMalwareContainedOrPurged()) {
                    audio.playGameOver();
                    triggerScreenShake(22.0, Color.web("#05D5FA"), 0.6);
                    loadNewScreen(Plan.GAME_OVER);
                }
            }
        }
    }

    private void performAiSocDefenseAction() {
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        int bestBlockX = -1, bestBlockY = -1;
        double minBorderDist = 999;

        List<int[]> malwareCells = new ArrayList<>();
        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                if (grid[y][x].getState().isMalware()) {
                    malwareCells.add(new int[]{x, y});
                    for (int[] d : dirs) {
                        int nx = x + d[0], ny = y + d[1];
                        if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                            if (grid[ny][nx].getState() == CellState.EMPTY) {
                                int distToEdge = Math.min(Math.min(nx, NBcases - 1 - nx), Math.min(ny, NBcases - 1 - ny));
                                if (distToEdge < minBorderDist || (distToEdge == minBorderDist && random.nextBoolean())) {
                                    minBorderDist = distToEdge;
                                    bestBlockX = nx;
                                    bestBlockY = ny;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestBlockX != -1 && random.nextDouble() < 0.78) {
            double cx = ORIGIN[0] + bestBlockX * LENcases + LENcases / 2.0;
            double cy = ORIGIN[1] + bestBlockY * LENcases + LENcases / 2.0;

            if (wave >= 2 && random.nextDouble() < 0.16 && countCells(CellState.TURRET) < 2 + wave) {
                grid[bestBlockY][bestBlockX].setState(CellState.TURRET);
                spawnExplosion(cx, cy, Color.web("#10B981"), 16, 120);
                spawnPopup(cx, cy, "🛰️ IA : SENTRY DÉPLOYÉE !", Color.web("#10B981"), 14, PopupStyle.HEX_TECH);
            } else {
                grid[bestBlockY][bestBlockX].setState(CellState.FIREWALL);
                spawnExplosion(cx, cy, Color.web("#05D5FA"), 10, 90);
            }
            return;
        }

        if (malwareCells.size() > 2) {
            int[] target = malwareCells.get(random.nextInt(malwareCells.size()));
            double tx = ORIGIN[0] + target[0] * LENcases + LENcases / 2.0;
            double ty = ORIGIN[1] + target[1] * LENcases + LENcases / 2.0;
            grid[target[1]][target[0]].setState(CellState.EMPTY);
            fireOrbitalLaser(tx, ty, Color.web("#05D5FA"));
            spawnExplosion(tx, ty, Color.web("#05D5FA"), 20, 180);
            spawnPopup(tx, ty, "⚡ FRAPPE IA SOC !", Color.web("#05D5FA"), 13, PopupStyle.NEON_FLOAT);
        }
    }

    private boolean hasRedTeamEscapedOrHackedAllCores() {
        if (countCells(CellState.DATA_CORE) == 0) {
            return true;
        }
        for (int i = 0; i < NBcases; i++) {
            if (grid[0][i].getState().isMalware() ||
                    grid[NBcases - 1][i].getState().isMalware() ||
                    grid[i][0].getState().isMalware() ||
                    grid[i][NBcases - 1].getState().isMalware()) {
                return true;
            }
        }
        return false;
    }

    private void triggerRedTeamVictory() {
        if (roundEnding) return;
        roundEnding = true;
        victoryDelayTimer = 2.0;
        virusMissiles.clear();
        audio.playPowerup();
        triggerScreenShake(26.0, Color.web("#FF2A6D"), 0.5);

        int infectedNodes = countTotalMalwareCells();
        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                CellState st = grid[y][x].getState();
                if (st == CellState.FIREWALL || st == CellState.DAMAGED_FIREWALL || st == CellState.TURRET) {
                    double cx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                    double cy = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                    grid[y][x].setState(CellState.VIRUS);
                    spawnExplosion(cx, cy, Color.web("#FF2A6D"), 18, 220);
                }
            }
        }

        int bonusXp = 700 + infectedNodes * 90;
        int bonusCredits = 260 + infectedNodes * 15;
        score += (doubleXpTimer > 0) ? bonusXp * 2 : bonusXp;
        credits += bonusCredits;

        awardMedal("☣️ MÉDAILLE [MAÎTRE INFILTRATEUR] : RÉSEAU SOC COMPROMIS !", 500, 150);
    }

    private int countTotalMalwareCells() {
        int count = 0;
        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                if (grid[y][x].getState().isMalware()) count++;
            }
        }
        return count;
    }

    public void updateLayoutMetrics() {
        double w = Math.max(850, scene.getWidth());
        double h = Math.max(600, scene.getHeight());

        LENcases = (int) Math.max(19, Math.min(28, (h - 230) / NBcases));

        int gridPixelSize = NBcases * LENcases;
        ORIGIN[0] = (int) ((w - gridPixelSize) / 2.0);
        ORIGIN[1] = (int) ((h - gridPixelSize) / 2.0) + 8;
    }

    public double getCurrentSpreadRate() {
        return BASE_SPREAD_RATE + (wave - 1) * WAVE_SPEED_INCREMENT;
    }

    private String pickActionWord() {
        if (comicMode) {
            return COMIC_WORDS[random.nextInt(COMIC_WORDS.length)];
        }
        return FUTURISTIC_WORDS[random.nextInt(FUTURISTIC_WORDS.length)];
    }

    private void applySelectedCyberDeck() {
        maxBandwidth = 150.0;
        bandwidthRegenPerSec = 36.0;
        hyperSurgeDuration = (selectedDeck == 0) ? 9.0 : 6.0;
        turretFireInterval = (selectedDeck == 2) ? 0.78 : 1.15;
        nukeCost = (selectedDeck == 1) ? 38.0 : 48.0;
        perimeterShields = (selectedDeck == 0) ? 2 : 1;
        droneCount = (selectedDeck == 2) ? 1 : 0;
        rollbackCharges = 2;

        upgBwLevel = 1;
        upgRegenLevel = 1;
        upgSurgeLevel = 1;
        orbitalCharge = 50.0;
        credits = 250;
        wave = 1;
    }

    // =========================================================================
    // IDÉE 1 : COMPRESSION DE QUARANTAINE PAR ENCERCLEMENT (ALGORITHME FLOOD-FILL)
    // =========================================================================

    public void checkFloodFillEncirclement() {
        if (roundEnding || redTeamMode) return;

        boolean[][] reachableFromBorder = new boolean[NBcases][NBcases];
        Queue<int[]> queue = new ArrayDeque<>();

        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                if (x == 0 || x == NBcases - 1 || y == 0 || y == NBcases - 1) {
                    if (!grid[y][x].getState().isBlockingWall()) {
                        reachableFromBorder[y][x] = true;
                        queue.add(new int[]{x, y});
                    }
                }
            }
        }

        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cx = curr[0], cy = curr[1];

            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                    if (!reachableFromBorder[ny][nx] && !grid[ny][nx].getState().isBlockingWall()) {
                        reachableFromBorder[ny][nx] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }

        int crushedViruses = 0;
        int capturedEmptyCells = 0;
        double sumX = 0, sumY = 0;

        for (int y = 1; y < NBcases - 1; y++) {
            for (int x = 1; x < NBcases - 1; x++) {
                if (!reachableFromBorder[y][x] && !grid[y][x].getState().isBlockingWall()) {
                    CellState st = grid[y][x].getState();
                    double cellX = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                    double cellY = ORIGIN[1] + y * LENcases + LENcases / 2.0;

                    if (st.isMalware()) {
                        grid[y][x].setState(CellState.FIREWALL);
                        crushedViruses++;
                        killCount++;
                        waveKills++;
                        orbitalCharge = Math.min(100.0, orbitalCharge + 10.0);
                        sumX += cellX;
                        sumY += cellY;
                        spawnExplosion(cellX, cellY, Color.web("#05D5FA"), 28, 260);
                        spawnExplosion(cellX, cellY, Color.web("#FACC15"), 14, 180);
                        shockwaves.add(new Shockwave(cellX, cellY, LENcases * 2.2, Color.web("#05D5FA")));
                    } else if (st == CellState.EMPTY || st == CellState.DAMAGED_FIREWALL) {
                        capturedEmptyCells++;
                    }
                }
            }
        }

        if (crushedViruses > 0) {
            if (capturedEmptyCells <= 30) {
                for (int y = 1; y < NBcases - 1; y++) {
                    for (int x = 1; x < NBcases - 1; x++) {
                        if (!reachableFromBorder[y][x] && (grid[y][x].getState() == CellState.EMPTY || grid[y][x].getState() == CellState.DAMAGED_FIREWALL)) {
                            grid[y][x].setState(CellState.FIREWALL);
                            double cx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                            double cy = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                            spawnExplosion(cx, cy, Color.web("#38BDF8"), 6, 90);
                        }
                    }
                }
            }

            double centerExplosionX = sumX / crushedViruses;
            double centerExplosionY = sumY / crushedViruses;

            audio.playNuke();
            triggerScreenShake(20.0, Color.web("#05D5FA"), 0.45);
            int territoryBonus = crushedViruses * 180 + capturedEmptyCells * 50;
            addScore(
                    territoryBonus,
                    40 + crushedViruses * 15,
                    centerExplosionX,
                    centerExplosionY,
                    "🌐 QUARANTAINE +" + territoryBonus + " XP !",
                    Color.web("#05D5FA"),
                    18,
                    PopupStyle.STARBURST
            );

            if (crushedViruses >= 3) {
                awardMedal("🌐 MÉDAILLE [MAÎTRE DE L'ENCERCLEMENT] : POCHE VIRALE COMPRESSÉE !", 700, 250);
            }

            if (isAllMalwareContainedOrPurged()) {
                triggerContainmentVictory();
            }
        }
    }

    // =========================================================================
    // IDÉE 2 : LE POT DE MIEL (HONEYPOT) & ANTIVIRUS RÉPLICANT (VERT FLUO)
    // =========================================================================

    private void updateAntivirusReplication(double dt) {
        if (roundEnding || redTeamMode) return;
        antivirusTickTimer += dt;
        if (antivirusTickTimer < 0.38) return;
        antivirusTickTimer = 0;

        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}, {-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        List<int[]> newAntivirusCells = new ArrayList<>();

        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                Cell cell = grid[y][x];
                if (cell.getState() == CellState.ANTIVIRUS) {
                    boolean ateMalware = false;

                    for (int[] d : dirs) {
                        int nx = x + d[0], ny = y + d[1];
                        if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                            CellState neighbor = grid[ny][nx].getState();
                            double tx = ORIGIN[0] + nx * LENcases + LENcases / 2.0;
                            double ty = ORIGIN[1] + ny * LENcases + LENcases / 2.0;

                            if (neighbor.isMalware()) {
                                newAntivirusCells.add(new int[]{nx, ny});
                                killCount++;
                                waveKills++;
                                ateMalware = true;
                                audio.playLaser();
                                spawnExplosion(tx, ty, Color.web("#00FF9D"), 22, 200);
                                shockwaves.add(new Shockwave(tx, ty, LENcases * 1.6, Color.web("#00FF9D")));
                                addScore(140, 25, tx, ty, "🧬 ANTIVIRUS +140 XP", Color.web("#00FF9D"), 14, PopupStyle.HEX_TECH);
                            } else if (neighbor == CellState.DAMAGED_FIREWALL) {
                                grid[ny][nx].setState(CellState.FIREWALL);
                            }
                        }
                    }

                    if (!ateMalware) {
                        cell.antivirusLife--;
                        if (cell.antivirusLife <= 0) {
                            cell.setState(CellState.FIREWALL);
                        }
                    }
                }
            }
        }

        for (int[] pos : newAntivirusCells) {
            Cell c = grid[pos[1]][pos[0]];
            c.setState(CellState.ANTIVIRUS);
            c.antivirusLife = 4;
        }

        if (!newAntivirusCells.isEmpty() && isAllMalwareContainedOrPurged()) {
            triggerContainmentVictory();
        }
    }

    private void triggerHoneypotRetroEngineering(int hx, int hy) {
        double cx = ORIGIN[0] + hx * LENcases + LENcases / 2.0;
        double cy = ORIGIN[1] + hy * LENcases + LENcases / 2.0;

        audio.playPowerup();
        triggerScreenShake(18.0, Color.web("#00FF9D"), 0.4);
        shockwaves.add(new Shockwave(cx, cy, LENcases * 3.5, Color.web("#00FF9D")));
        spawnExplosion(cx, cy, Color.web("#00FF9D"), 50, 300);

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = hx + dx, ny = hy + dy;
                if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                    CellState st = grid[ny][nx].getState();
                    if (st == CellState.EMPTY || st == CellState.HONEYPOT || st.isMalware()) {
                        if (st.isMalware()) {
                            killCount++;
                            waveKills++;
                        }
                        grid[ny][nx].setState(CellState.ANTIVIRUS);
                        grid[ny][nx].antivirusLife = 5;
                    }
                }
            }
        }

        addScore(350, 80, cx, cy, "🧬 ANTIVIRUS LIBÉRÉ !", Color.web("#00FF9D"), 17, PopupStyle.HEX_TECH);
    }

    // =========================================================================
    // IDÉE 3 : ROLLBACK SYSTÈME [R]
    // =========================================================================

    private void recordGridSnapshot() {
        GridSnapshot snap = new GridSnapshot(grid);
        snapshotHistory.addLast(snap);
        while (snapshotHistory.size() > 5) {
            snapshotHistory.removeFirst();
        }
    }

    public void activateSystemRollback() {
        if (currentPlan != Plan.GAMEPLAY || roundEnding) return;
        if (rollbackCharges <= 0) {
            spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 20, "⚠️ AUCUN ROLLBACK DISPONIBLE !", Color.web("#FF2A6D"), 15, PopupStyle.WARNING_SHIELD);
            return;
        }
        if (snapshotHistory.isEmpty()) {
            spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 20, "⏳ SNAPSHOT EN COURS...", Color.web("#38BDF8"), 15, PopupStyle.HEX_TECH);
            return;
        }

        rollbackCharges--;
        GridSnapshot targetSnap = snapshotHistory.getFirst();
        snapshotHistory.clear();

        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                CellState current = grid[y][x].getState();
                CellState oldState = targetSnap.states[y][x];

                if (redTeamMode) {
                    if (current.isMalware()) continue;
                    if ((current == CellState.FIREWALL || current == CellState.TURRET) && oldState == CellState.EMPTY) {
                        double cx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                        double cy = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                        grid[y][x].setState(CellState.EMPTY);
                        spawnExplosion(cx, cy, Color.web("#FF2A6D"), 12, 150);
                    } else if (oldState.isMalware()) {
                        grid[y][x].setState(oldState);
                    }
                } else {
                    if (current == CellState.FIREWALL || current == CellState.TURRET || current == CellState.PLASMA_MINE
                            || current == CellState.HONEYPOT || current == CellState.ANTIVIRUS) {
                        continue;
                    }
                    if (current.isMalware() && !oldState.isMalware()) {
                        double cx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                        double cy = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                        spawnExplosion(cx, cy, Color.web("#38BDF8"), 10, 140);
                    }
                    grid[y][x].setState(oldState);
                }
            }
        }

        if (!redTeamMode) {
            for (VirusMissile vm : virusMissiles) {
                spawnExplosion(vm.x, vm.y, Color.web("#38BDF8"), 20, 200);
            }
            virusMissiles.clear();
        }

        rewindEffectTimer = 1.1;
        audio.playRollback();
        triggerScreenShake(16.0, redTeamMode ? Color.web("#FF2A6D") : Color.web("#38BDF8"), 0.45);
        spawnPopup(
                scene.getWidth() / 2.0,
                ORIGIN[1] - 25,
                redTeamMode ? "⏪ POLYMORPHE (-4.0s) ACTIVÉ !" : "⏪ ROLLBACK (-4.0s) ACTIVÉ !",
                redTeamMode ? Color.web("#FF2A6D") : Color.web("#38BDF8"),
                18,
                PopupStyle.HEX_TECH
        );
    }

    private void updateHyperSurge(double dt) {
        if (roundEnding) return;

        if (hyperSurgeTimer > 0) {
            hyperSurgeTimer -= dt;
            if (hyperSurgeTimer <= 0) {
                hyperSurgeTimer = 0;
                spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 22, "FIN DU MODE SURCHARGE", Color.web("#94A3B8"), 15, PopupStyle.HEX_TECH);
            }
        } else {
            hyperSurgeCharge += dt;
            if (hyperSurgeCharge >= HYPER_SURGE_COOLDOWN_MAX) {
                hyperSurgeCharge = 0;
                hyperSurgeTimer = hyperSurgeDuration;
                bandwidth = maxBandwidth;
                audio.playPowerup();
                triggerScreenShake(14.0, Color.web("#FACC15"), 0.4);
                spawnPopup(
                        scene.getWidth() / 2.0,
                        ORIGIN[1] - 25,
                        "⚡ SURCHARGE (" + (int) hyperSurgeDuration + "s) : GLISSE LA SOURIS ! ⚡",
                        Color.web("#FACC15"),
                        18,
                        PopupStyle.HEX_TECH
                );
            }
        }
    }

    private void updateWeatherAndAirdrops(double dt) {
        if (roundEnding) return;

        if (wave >= 4) {
            airdropSpawnTimer -= dt;
            if (airdropSpawnTimer <= 0) {
                airdropSpawnTimer = 11.0;
                double dropX = ORIGIN[0] + 40 + random.nextDouble() * (NBcases * LENcases - 80);
                supplyDrops.add(new SupplyDrop(dropX, ORIGIN[1] - 30));
            }
        }

        Iterator<SupplyDrop> sdIt = supplyDrops.iterator();
        while (sdIt.hasNext()) {
            SupplyDrop sd = sdIt.next();
            sd.update(dt);
            if (sd.y > ORIGIN[1] + NBcases * LENcases + 30) {
                sdIt.remove();
            }
        }

        if (wave >= 5) {
            nextWeatherEventTimer -= dt;
            if (nextWeatherEventTimer <= 0) {
                nextWeatherEventTimer = 25.0;
                blackoutTimer = 5.0;
                audio.playPowerup();
                spawnPopup(
                        scene.getWidth() / 2.0,
                        ORIGIN[1] - 25,
                        "🌑 BLACKOUT (5s) : BONUS XP +50% ! 🌑",
                        Color.web("#A855F7"),
                        18,
                        PopupStyle.WARNING_SHIELD
                );
            }
        }
    }

    public void activateRedButtonOrbitalBeam() {
        if (currentPlan != Plan.GAMEPLAY || roundEnding) return;
        if (!redTeamMode && wave < 2) {
            spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 20, "🔒 DÉBLOQUÉ AU NIVEAU 2 !", Color.web("#FACC15"), 16, PopupStyle.WARNING_SHIELD);
            return;
        }
        if (orbitalCharge < 100.0 || orbitalBeamActive) {
            spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 20, "BOUTON ROUGE EN CHARGE (" + (int) orbitalCharge + "%)", Color.web("#FF2A6D"), 16, PopupStyle.HEX_TECH);
            return;
        }

        orbitalCharge = 0;
        orbitalBeamActive = true;
        orbitalBeamY = ORIGIN[1];
        lastOrbitalRowSwept = -1;
        audio.playOrbitalBeam();
        triggerScreenShake(28.0, Color.web("#FF2A6D"), 0.55);
        spawnPopup(
                scene.getWidth() / 2.0,
                ORIGIN[1] - 25,
                redTeamMode ? "☣️ ONDE ZERO-DAY ACTIVÉE ! ☣️" : "☢️ RAYON ORBITAL ACTIVÉ ! ☢️",
                Color.web("#FACC15"),
                20,
                PopupStyle.STARBURST
        );

        if (!redTeamMode) {
            for (VirusMissile vm : virusMissiles) {
                spawnExplosion(vm.x, vm.y, Color.web("#FACC15"), 30, 260);
            }
            virusMissiles.clear();
        }
    }

    private void updateOrbitalSweep(double dt) {
        if (!orbitalBeamActive) return;

        double sweepSpeed = 300.0;
        orbitalBeamY += sweepSpeed * dt;
        shakeIntensity = Math.max(shakeIntensity, 10.0);
        audioEnergy = 1.0;

        int currentRow = (int) Math.floor((orbitalBeamY - ORIGIN[1]) / LENcases);
        if (currentRow >= 0 && currentRow < NBcases && currentRow != lastOrbitalRowSwept) {
            lastOrbitalRowSwept = currentRow;

            for (int x = 0; x < NBcases; x++) {
                double cx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                double cy = ORIGIN[1] + currentRow * LENcases + LENcases / 2.0;
                CellState st = grid[currentRow][x].getState();

                if (x % 2 == 0) {
                    spawnExplosion(cx, cy, Color.web("#FACC15"), 6, 130);
                }

                if (redTeamMode) {
                    if (st == CellState.FIREWALL || st == CellState.DAMAGED_FIREWALL || st == CellState.TURRET) {
                        grid[currentRow][x].setState(random.nextDouble() < 0.35 ? CellState.VIRUS : CellState.EMPTY);
                        killCount++;
                        score += 110;
                        credits += 15;
                        spawnExplosion(cx, cy, Color.web("#05D5FA"), 22, 240);
                        shockwaves.add(new Shockwave(cx, cy, LENcases * 1.8, Color.web("#FF2A6D")));
                    }
                } else {
                    if (st.isMalware()) {
                        grid[currentRow][x].setState(CellState.EMPTY);
                        killCount++;
                        waveKills++;
                        score += 120;
                        credits += 15;
                        spawnExplosion(cx, cy, Color.web("#FF2A6D"), 24, 260);
                        shockwaves.add(new Shockwave(cx, cy, LENcases * 1.8, Color.web("#FACC15")));
                    } else if (st == CellState.DAMAGED_FIREWALL) {
                        grid[currentRow][x].setState(CellState.FIREWALL);
                    }
                }
            }
        }

        if (orbitalBeamY >= ORIGIN[1] + NBcases * LENcases) {
            orbitalBeamActive = false;
            if (!redTeamMode && isAllMalwareContainedOrPurged()) {
                triggerContainmentVictory();
            } else if (redTeamMode && hasRedTeamEscapedOrHackedAllCores()) {
                triggerRedTeamVictory();
            }
        }
    }

    public void loadNewScreen(Plan plan) {
        updateLayoutMetrics();
        pane.getChildren().clear();
        pane.getChildren().add(bgCanvas);
        currentPlan = plan;

        if (plan == Plan.GAMEPLAY) {
            comicMode = (wave % 2 == 0);
        }

        bgContext.setFill(comicMode ? Color.web("#FDF6E2") : Color.web("#050811"));
        bgContext.fillRect(0, 0, scene.getWidth(), scene.getHeight());

        switch (currentPlan) {
            case ACCUEIL -> {
                audio.setMusicActive(false);
                addAccueilWithModeAndDeckSelection();
            }
            case GAMEPLAY -> {
                audio.setMusicActive(true);
                initGrid();
            }
            case SHOP, VICTORY -> {
                audio.playPowerup();
                renderGame();
                addCyberShopOverlay();
            }
            case GAME_OVER -> {
                audio.setMusicActive(false);
                renderGame();
                addGameOverOverlay();
            }
        }
    }

    public void addAccueilWithModeAndDeckSelection() {
        VBox vBox = new VBox(14);
        vBox.setAlignment(Pos.CENTER);
        vBox.prefWidthProperty().bind(scene.widthProperty());
        vBox.prefHeightProperty().bind(scene.heightProperty());
        vBox.setStyle("-fx-background-color: rgba(5, 8, 17, 0.94);");

        try {
            ImageView virus = new ImageView(new Image("virus.png"));
            virus.setPreserveRatio(true);
            virus.setFitHeight(52);
            vBox.getChildren().add(virus);
        } catch (Exception ignored) {
        }

        Label title = new Label("// CYBER-CONTAINMENT : DUAL PROTOCOL //");
        title.setTextFill(Color.web("#05D5FA"));
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 36));

        Label modeLabel = new Label("1. CHOISIS TON CAMP :");
        modeLabel.setTextFill(Color.web("#FACC15"));
        modeLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 16));

        HBox modeBox = new HBox(20);
        modeBox.setAlignment(Pos.CENTER);

        Button btnBlueTeam = new Button("🔵 MODE BLUE TEAM (DÉFENSEUR SOC)\nContiens et purge le virus avec tes Firewalls,\nPots de Miel, Tourelles Tesla et Lasers !");
        Button btnRedTeam = new Button("🔴 MODE RED TEAM (INFILTRATION VIRUS)\nTU ES LE VIRUS ! Brise les Firewalls de l'IA,\npirate les Serveurs et échappe-toi du réseau !");

        HBox decksBox = new HBox(18);
        decksBox.setAlignment(Pos.CENTER);

        Button btnDeck0 = new Button();
        Button btnDeck1 = new Button();
        Button btnDeck2 = new Button();
        Button[] deckBtns = {btnDeck0, btnDeck1, btnDeck2};
        String[] deckColors = {"#05D5FA", "#FACC15", "#10B981"};

        Button btnPlay = new Button();
        btnPlay.setFont(Font.font("Consolas", FontWeight.BOLD, 20));

        Runnable refreshMenuUI = () -> {
            btnBlueTeam.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
            btnBlueTeam.setPrefSize(430, 88);
            btnBlueTeam.setStyle(
                    "-fx-background-color: " + (!redTeamMode ? "#0C4A6E" : "#0F172A") + "; " +
                            "-fx-text-fill: " + (!redTeamMode ? "#38BDF8" : "#64748B") + "; " +
                            "-fx-border-color: " + (!redTeamMode ? "#05D5FA" : "#334155") + "; " +
                            "-fx-border-width: " + (!redTeamMode ? "3.5" : "1.5") + "; " +
                            "-fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;"
            );

            btnRedTeam.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
            btnRedTeam.setPrefSize(430, 88);
            btnRedTeam.setStyle(
                    "-fx-background-color: " + (redTeamMode ? "#4C0519" : "#0F172A") + "; " +
                            "-fx-text-fill: " + (redTeamMode ? "#FF2A6D" : "#64748B") + "; " +
                            "-fx-border-color: " + (redTeamMode ? "#FF2A6D" : "#334155") + "; " +
                            "-fx-border-width: " + (redTeamMode ? "3.5" : "1.5") + "; " +
                            "-fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;"
            );

            if (redTeamMode) {
                btnDeck0.setText("👑 SOUCHE MULTI-FOYERS\nCommence avec 2 foyers viraux\nSurcharge Virale de 9s\nRésistance accrue à l'IA");
                btnDeck1.setText("⚡ SOUCHE OVERCLOCK\nPropagation Virale +30% rapide\nBombe Logique 3x3 coûte 38 TF\nMutation agressive");
                btnDeck2.setText("🚀 SOUCHE BOTNET\nMissiles Viraux plus rapides\n+1 Drone Pirate dès Niveau 1\nRégénération TFLOPS boostée");
                btnPlay.setText(">> LANCER L'INFILTRATION RED TEAM (NIVEAU 1) <<");
                btnPlay.setStyle("-fx-background-color: #FF2A6D; -fx-text-fill: white; -fx-padding: 14 44 14 44; -fx-background-radius: 8; -fx-cursor: hand;");
            } else {
                btnDeck0.setText("🛡️ DECK ARCHITECTE\n2 Boucliers Périmètre\nHyper-Firewall de 9s\nMurs plus solides");
                btnDeck1.setText("⚡ DECK NETRUNNER\nLasers Éclairs en Chaîne\nNuke 3x3 coûte 38 BW\nSurcharge Offensive");
                btnDeck2.setText("🤖 DECK TECHNOMANCIEN\n1 Drone Volant dès Niveau 1\nTourelles +35% rapides\nBarrières Tesla Boostées");
                btnPlay.setText(">> LANCER LA DÉFENSE BLUE TEAM (NIVEAU 1) <<");
                btnPlay.setStyle("-fx-background-color: #05D5FA; -fx-text-fill: #050811; -fx-padding: 14 44 14 44; -fx-background-radius: 8; -fx-cursor: hand;");
            }

            for (int i = 0; i < 3; i++) {
                boolean active = (selectedDeck == i);
                deckBtns[i].setFont(Font.font("Consolas", FontWeight.BOLD, 13));
                deckBtns[i].setPrefSize(290, 110);
                deckBtns[i].setStyle(
                        "-fx-background-color: " + (active ? "#1E293B" : "#0F172A") + "; " +
                                "-fx-text-fill: " + (active ? deckColors[i] : "#94A3B8") + "; " +
                                "-fx-border-color: " + (active ? deckColors[i] : "#334155") + "; " +
                                "-fx-border-width: " + (active ? "3" : "1.5") + "; " +
                                "-fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;"
                );
            }
        };

        btnBlueTeam.setOnAction(e -> {
            redTeamMode = false;
            audio.playPowerup();
            refreshMenuUI.run();
        });
        btnRedTeam.setOnAction(e -> {
            redTeamMode = true;
            audio.playPowerup();
            refreshMenuUI.run();
        });

        for (int i = 0; i < 3; i++) {
            final int deckIdx = i;
            deckBtns[i].setOnAction(e -> {
                selectedDeck = deckIdx;
                audio.playPowerup();
                refreshMenuUI.run();
            });
        }

        refreshMenuUI.run();
        modeBox.getChildren().addAll(btnBlueTeam, btnRedTeam);

        Label deckLabel = new Label("2. CHOISIS TA SPÉCIALISATION :");
        deckLabel.setTextFill(Color.web("#E2E8F0"));
        deckLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        decksBox.getChildren().addAll(btnDeck0, btnDeck1, btnDeck2);

        btnPlay.setOnAction(event -> {
            score = 0;
            killCount = 0;
            totalMedalsEarned = 0;
            currentRankIndex = 0;
            applySelectedCyberDeck();
            loadNewScreen(Plan.GAMEPLAY);
        });

        // Bouton pour basculer vers le second jeu : NanoSondeGame
        Button btnNanoSonde = new Button("JOUER À « NANO-SONDE : FLUX SANGUIN / FIBRE OPTIQUE » (360° & MITOSE) 🧬");
        btnNanoSonde.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        btnNanoSonde.setStyle("-fx-background-color: #10B981; -fx-text-fill: #050811; -fx-padding: 10 28 10 28; -fx-background-radius: 8; -fx-cursor: hand;");
        btnNanoSonde.setOnAction(e -> {
            audio.stopEngine();
            Stage currentStage = (Stage) scene.getWindow();
            NanoSondeGame.launchInStage(currentStage, () -> {
                try {
                    new HelloApplication().start(currentStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        vBox.getChildren().addAll(title, modeLabel, modeBox, deckLabel, decksBox, btnPlay, btnNanoSonde);
        pane.getChildren().add(vBox);
    }

    public void addGameOverOverlay() {
        VBox vBox = new VBox(20);
        vBox.setAlignment(Pos.CENTER);
        vBox.prefWidthProperty().bind(scene.widthProperty());
        vBox.prefHeightProperty().bind(scene.heightProperty());
        vBox.setStyle("-fx-background-color: rgba(5, 8, 17, 0.90);");

        String overTitle = redTeamMode
                ? "// ÉCHEC DE L'INFILTRATION : L'IA SOC A ÉRADIQUÉ TON VIRUS //"
                : "// BRÈCHE CRITIQUE DÉTECTÉE //";
        Label title = new Label(overTitle);
        title.setTextFill(Color.web("#FF2A6D"));
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 34));

        Label subtitle = new Label("RANG : " + RANK_NAMES[currentRankIndex] + "  |  SCORE : " + score + " XP  |  MÉDAILLES : " + totalMedalsEarned);
        subtitle.setTextFill(Color.web("#E2E8F0"));
        subtitle.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        Button btnRestart = new Button(">> RETOUR AU CHOIX DU MODE (BLUE TEAM / RED TEAM) <<");
        btnRestart.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        btnRestart.setStyle("-fx-background-color: #05D5FA; -fx-text-fill: #050811; -fx-padding: 14 38 14 38; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRestart.setOnAction(e -> loadNewScreen(Plan.ACCUEIL));

        vBox.getChildren().addAll(title, subtitle, btnRestart);
        pane.getChildren().add(vBox);
    }

    public void addCyberShopOverlay() {
        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        root.setStyle("-fx-background-color: rgba(5, 8, 17, 0.92);");

        String nextStyleName = (wave % 2 == 0) ? "STYLE BANDE DESSINÉE" : "STYLE FUTURISTE";
        String shopHeader = redTeamMode
                ? "// LABORATOIRE DE MUTATION RED TEAM [NIVEAU " + (wave - 1) + " INFILTRÉ] //"
                : "// BOUTIQUE D'AMÉLIORATIONS SOC [NIVEAU " + (wave - 1) + " COMPLÉTÉ] //";

        Label title = new Label(shopHeader);
        title.setTextFill(redTeamMode ? Color.web("#FF2A6D") : Color.web("#05D5FA"));
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 28));

        Label statsLabel = new Label();
        statsLabel.setTextFill(Color.web("#FACC15"));
        statsLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        Runnable refreshLabels = () -> statsLabel.setText(
                "💳 CRÉDITS : " + credits + " $   |   ⏪ ROLLBACKS : " + rollbackCharges + "   |   🤖 DRONES : " + droneCount + "   |   🎖️ RANG : " + RANK_NAMES[currentRankIndex]
        );
        refreshLabels.run();

        VBox itemsBox = new VBox(8);
        itemsBox.setAlignment(Pos.CENTER);

        itemsBox.getChildren().add(createShopRow(
                redTeamMode ? "⚡ PUISSANCE TFLOPS MAX (+35 TF)" : "⚡ CAPACITÉ BANDWIDTH MAX (+35 GB/s)",
                () -> "Niv." + upgBwLevel + " (" + (int) maxBandwidth + ")",
                () -> 150 + (upgBwLevel - 1) * 100,
                () -> {
                    maxBandwidth += 35.0;
                    upgBwLevel++;
                    refreshLabels.run();
                }
        ));

        itemsBox.getChildren().add(createShopRow(
                redTeamMode ? "🔋 OVERCLOCKING PROCESSEUR (+12 TF/s)" : "🔋 GÉNÉRATEUR TURBO (+12 BW/s)",
                () -> "Niv." + upgRegenLevel + " (" + (int) bandwidthRegenPerSec + "/s)",
                () -> 160 + (upgRegenLevel - 1) * 110,
                () -> {
                    bandwidthRegenPerSec += 12.0;
                    upgRegenLevel++;
                    refreshLabels.run();
                }
        ));

        itemsBox.getChildren().add(createShopRow(
                "⏪ RECHARGE ROLLBACK / POLYMORPHE (+2 Utilisations [R])",
                () -> "Charges : " + rollbackCharges,
                () -> 140,
                () -> {
                    rollbackCharges += 2;
                    refreshLabels.run();
                }
        ));

        itemsBox.getChildren().add(createShopRow(
                "🤖 CONSTRUIRE UN DRONE AUTONOME (+1 Drone)",
                () -> "Escouade : " + droneCount + " Drones",
                () -> 200 + droneCount * 90,
                () -> {
                    droneCount++;
                    refreshLabels.run();
                }
        ));

        itemsBox.getChildren().add(createShopRow(
                "☢️ CHARGE IMMÉDIATE BOUTON ROUGE 100% (+1 Bouclier)",
                () -> "Bouton Rouge : " + (int) orbitalCharge + "%",
                () -> 200,
                () -> {
                    perimeterShields++;
                    orbitalCharge = 100.0;
                    refreshLabels.run();
                }
        ));

        Button btnNextWave = new Button(">> LANCER LE NIVEAU " + wave + " (" + nextStyleName + ") <<");
        btnNextWave.setFont(Font.font("Consolas", FontWeight.BOLD, 19));
        btnNextWave.setStyle(
                "-fx-background-color: #4ADE80; -fx-text-fill: #050811; " +
                        "-fx-padding: 12 36 12 36; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnNextWave.setOnAction(e -> loadNewScreen(Plan.GAMEPLAY));

        root.getChildren().addAll(title, statsLabel, itemsBox, btnNextWave);
        pane.getChildren().add(root);
    }

    private HBox createShopRow(String name, java.util.function.Supplier<String> statusSupplier, java.util.function.Supplier<Integer> costSupplier, Runnable onBuy) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(820);
        row.setMaxWidth(820);
        row.setStyle("-fx-background-color: #0F172A; -fx-border-color: #05D5FA; -fx-border-width: 1.5; -fx-padding: 10 18 10 18; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label lblName = new Label(name);
        lblName.setTextFill(Color.WHITE);
        lblName.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        lblName.setPrefWidth(440);

        Label lblStatus = new Label(statusSupplier.get());
        lblStatus.setTextFill(Color.web("#38BDF8"));
        lblStatus.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        lblStatus.setPrefWidth(170);

        Button btnBuy = new Button("ACHETER (" + costSupplier.get() + " $)");
        btnBuy.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        btnBuy.setStyle("-fx-background-color: #FACC15; -fx-text-fill: black; -fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand;");

        btnBuy.setOnAction(e -> {
            int cost = costSupplier.get();
            if (credits >= cost) {
                credits -= cost;
                audio.playPowerup();
                onBuy.run();
                lblStatus.setText(statusSupplier.get());
                btnBuy.setText("ACHETER (" + costSupplier.get() + " $)");
            } else {
                btnBuy.setStyle("-fx-background-color: #FF2A6D; -fx-text-fill: white; -fx-padding: 7 16 7 16; -fx-background-radius: 6;");
            }
        });

        row.getChildren().addAll(lblName, lblStatus, btnBuy);
        return row;
    }

    public void initGrid() {
        updateLayoutMetrics();
        time = 0;
        spreadAccumulator = 0;
        snapshotTimer = 0;
        rewindEffectTimer = 0;
        aiDefenseTimer = 0.55;
        bandwidth = maxBandwidth;
        waveKills = 0;
        missilesIntercepted = 0;
        combo = 0;
        comboTimer = 0;
        doubleXpTimer = 0;
        empFreezeTimer = 0;
        blackoutTimer = 0;
        nextWeatherEventTimer = 18.0;
        airdropSpawnTimer = 9.0;
        hyperSurgeCharge = 0;
        hyperSurgeTimer = 0;
        orbitalBeamActive = false;
        missileSpawnTimer = 2.8;
        selectedTool = 1;
        roundEnding = false;
        shakeIntensity = 0;

        particles.clear();
        floatingTexts.clear();
        shockwaves.clear();
        lasers.clear();
        virusMissiles.clear();
        swarmProjectiles.clear();
        supplyDrops.clear();
        drones.clear();
        activeTeslaLinks.clear();
        snapshotHistory.clear();

        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                grid[y][x] = new Cell();
            }
        }

        if (redTeamMode) {
            spawnRedTeamWaveContent();
        } else {
            spawnProgressiveWaveContent();
        }

        recordGridSnapshot();
        renderGame();
    }

    public void spawnRedTeamWaveContent() {
        int mid = NBcases / 2 - 1;
        // Aucune cellule bloquée : toutes les cellules sont des virus actifs !
        grid[mid][mid].setState(CellState.VIRUS);
        grid[mid][mid + 1].setState(CellState.VIRUS);
        grid[mid + 1][mid].setState(CellState.TROJAN);
        grid[mid + 1][mid + 1].setState(CellState.VIRUS);

        if (selectedDeck == 0) {
            grid[mid - 2][mid].setState(CellState.WORM);
            grid[mid + 3][mid + 1].setState(CellState.TROJAN);
        }

        int[][] coreCoords = {{3, 3}, {NBcases - 4, 3}, {NBcases / 2, NBcases - 4}};
        for (int[] c : coreCoords) {
            grid[c[1]][c[0]].setState(CellState.DATA_CORE);
        }

        int[][] initialAiWalls = {{mid - 2, mid}, {mid + 3, mid + 1}, {mid, mid - 2}, {mid + 1, mid + 3}};
        for (int[] w : initialAiWalls) {
            if (grid[w[1]][w[0]].getState() == CellState.EMPTY) {
                grid[w[1]][w[0]].setState(CellState.FIREWALL);
            }
        }

        if (wave >= 2) {
            grid[3][NBcases / 2].setState(CellState.TURRET);
        }

        portalsUnlocked = (wave >= 3);
        if (portalsUnlocked) {
            portalAX = 3;
            portalAY = NBcases / 2;
            portalBX = NBcases - 4;
            portalBY = NBcases / 2;
            grid[portalAY][portalAX].setState(CellState.PORTAL_A);
            grid[portalBY][portalBX].setState(CellState.PORTAL_B);
        }
    }

    // Plus aucun bloc bloqué au milieu : génère des foyers d'infection normaux de plus en plus nombreux à chaque vague !
    public void spawnProgressiveWaveContent() {
        int mid = NBcases / 2 - 1;

        // Foyer principal au centre (100% destructible !)
        grid[mid][mid].setState(CellState.VIRUS);
        grid[mid][mid + 1].setState(CellState.VIRUS);
        grid[mid + 1][mid].setState(wave >= 2 ? CellState.TROJAN : CellState.VIRUS);
        grid[mid + 1][mid + 1].setState(wave >= 3 ? CellState.WORM : CellState.VIRUS);

        // Foyers d'infection supplémentaires selon le niveau pour augmenter la difficulté !
        int extraOutbreaks = Math.min(4, wave - 1);
        int[][] outbreakSpots = {{5, 6}, {14, 6}, {6, 14}, {14, 14}};
        for (int i = 0; i < extraOutbreaks; i++) {
            int ox = outbreakSpots[i][0];
            int oy = outbreakSpots[i][1];
            grid[oy][ox].setState(pickRandomStrain());
            grid[oy][ox + 1].setState(CellState.VIRUS);
        }

        // Serveurs DATA_CORE
        int[][] coreCoords = {{4, 4}, {NBcases - 5, 4}, {NBcases / 2, NBcases - 4}};
        for (int[] c : coreCoords) {
            grid[c[1]][c[0]].setState(CellState.DATA_CORE);
        }

        grid[mid][mid - 2].setState(CellState.HONEYPOT);

        if (wave >= 2) {
            grid[mid + 1][mid + 4].setState(CellState.PLASMA_MINE);
        }

        portalsUnlocked = (wave >= 4);
        if (portalsUnlocked) {
            portalAX = 3;
            portalAY = NBcases / 2;
            portalBX = NBcases - 4;
            portalBY = NBcases / 2;
            grid[portalAY][portalAX].setState(CellState.PORTAL_A);
            grid[portalBY][portalBX].setState(CellState.PORTAL_B);
        }

        int activeDronesThisWave = (wave >= 4) ? droneCount : (selectedDeck == 2 ? 1 : 0);
        double centerPx = ORIGIN[0] + (NBcases * LENcases) / 2.0;
        double centerPy = ORIGIN[1] + (NBcases * LENcases) / 2.0;
        for (int i = 0; i < activeDronesThisWave; i++) {
            boolean isHunter = (i % 2 == 0);
            drones.add(new AllyDrone(centerPx, centerPy, i * (Math.PI * 2 / Math.max(1, activeDronesThisWave)), isHunter));
        }
    }

    public void setupControls(Stage stage) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                return;
            }
            if (code == KeyCode.M) {
                audio.toggleMute();
                return;
            }
            if (code == KeyCode.T) {
                comicMode = !comicMode;
                audio.playPowerup();
                spawnPopup(
                        scene.getWidth() / 2.0,
                        ORIGIN[1] - 25,
                        comicMode ? "💥 STYLE BANDE DESSINÉE ACTIVÉ !" : "⚡ STYLE FUTURISTE ACTIVÉ !",
                        comicMode ? Color.web("#FACC15") : Color.web("#05D5FA"),
                        18,
                        comicMode ? PopupStyle.STARBURST : PopupStyle.HEX_TECH
                );
                return;
            }
            if (currentPlan != Plan.GAMEPLAY) return;

            if (code == KeyCode.SPACE) {
                activateRedButtonOrbitalBeam();
            } else if (code == KeyCode.R) {
                activateSystemRollback();
            } else if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) selectedTool = 1;
            else if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) selectedTool = 2;
            else if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
                if (redTeamMode || wave >= 3) activateEmpOrDdos();
                else spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 18, "🔒 EMP DÉBLOQUÉ AU NIVEAU 3 !", Color.web("#FACC15"), 15, PopupStyle.WARNING_SHIELD);
            } else if (code == KeyCode.DIGIT4 || code == KeyCode.NUMPAD4) {
                if (redTeamMode || wave >= 3) selectedTool = 4;
                else spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 18, "🔒 TOURELLES DÉBLOQUÉES AU NIVEAU 3 !", Color.web("#FACC15"), 15, PopupStyle.WARNING_SHIELD);
            } else if (code == KeyCode.DIGIT5 || code == KeyCode.NUMPAD5) {
                if (redTeamMode || wave >= 2) selectedTool = 5;
                else spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 18, "🔒 MINES PLASMA DÉBLOQUÉES AU NIVEAU 2 !", Color.web("#FACC15"), 15, PopupStyle.WARNING_SHIELD);
            } else if (code == KeyCode.DIGIT6 || code == KeyCode.NUMPAD6) {
                if (redTeamMode) launchRedTeamMissileSalvo();
                else selectedTool = 6;
            }
        });

        bgCanvas.setOnMouseMoved(event -> updateHover(event.getX(), event.getY()));

        bgCanvas.setOnMousePressed(event -> {
            double mx = event.getX();
            double my = event.getY();
            updateHover(mx, my);

            if (tryCollectSupplyDrop(mx, my)) return;
            if (!redTeamMode && tryInterceptVirusMissile(mx, my)) return;

            double rightPanelX = ORIGIN[0] + NBcases * LENcases + 14;
            double redBtnY = ORIGIN[1] + 280;
            if (mx >= rightPanelX && mx <= rightPanelX + 256 && my >= redBtnY && my <= redBtnY + 58) {
                activateRedButtonOrbitalBeam();
                return;
            }
            double rollbackBtnY = ORIGIN[1] + 346;
            if (mx >= rightPanelX && mx <= rightPanelX + 256 && my >= rollbackBtnY && my <= rollbackBtnY + 54) {
                activateSystemRollback();
                return;
            }

            double hotbarY = ORIGIN[1] + NBcases * LENcases + 16;
            if (my >= hotbarY && my <= hotbarY + 68) {
                handleHotbarClick(mx);
                return;
            }

            handleGridInteraction(hoverX, hoverY, event.getButton(), false);
        });

        bgCanvas.setOnMouseDragged(event -> {
            double mx = event.getX();
            double my = event.getY();
            updateHover(mx, my);

            tryCollectSupplyDrop(mx, my);
            if (!redTeamMode) tryInterceptVirusMissile(mx, my);
            if (selectedTool == 1 || hyperSurgeTimer > 0) {
                handleGridInteraction(hoverX, hoverY, event.getButton(), true);
            }
        });
    }

    private boolean tryCollectSupplyDrop(double mx, double my) {
        Iterator<SupplyDrop> it = supplyDrops.iterator();
        while (it.hasNext()) {
            SupplyDrop sd = it.next();
            if (Math.hypot(mx - sd.x, my - sd.y) <= 34.0) {
                it.remove();
                audio.playPowerup();
                bandwidth = maxBandwidth;
                orbitalCharge = Math.min(100.0, orbitalCharge + 35.0);
                spawnExplosion(sd.x, sd.y, Color.web("#FACC15"), 45, 300);
                shockwaves.add(new Shockwave(sd.x, sd.y, 95, Color.web("#FACC15")));
                addScore(350, 120, sd.x, sd.y, "📦 AIRDROP +120$ !", Color.web("#FACC15"), 16, PopupStyle.SPEECH_BUBBLE);
                return true;
            }
        }
        return false;
    }

    private void handleHotbarClick(double mx) {
        double cardW = 166;
        double gap = 10;
        double totalW = 6 * cardW + 5 * gap;
        double startX = (scene.getWidth() - totalW) / 2.0;

        for (int i = 0; i < 6; i++) {
            double x0 = startX + i * (cardW + gap);
            if (mx >= x0 && mx <= x0 + cardW) {
                int clickedTool = i + 1;
                if (clickedTool == 3) {
                    if (redTeamMode || wave >= 3) activateEmpOrDdos();
                    else spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 18, "🔒 EMP DÉBLOQUÉ AU NIVEAU 3 !", Color.web("#FACC15"), 15, PopupStyle.WARNING_SHIELD);
                } else if (clickedTool == 6 && redTeamMode) {
                    launchRedTeamMissileSalvo();
                } else if (!redTeamMode && clickedTool == 4 && wave < 3) {
                    spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 18, "🔒 TOURELLES DÉBLOQUÉES AU NIVEAU 3 !", Color.web("#FACC15"), 15, PopupStyle.WARNING_SHIELD);
                } else if (!redTeamMode && clickedTool == 5 && wave < 2) {
                    spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 18, "🔒 MINES PLASMA DÉBLOQUÉES AU NIVEAU 2 !", Color.web("#FACC15"), 15, PopupStyle.WARNING_SHIELD);
                } else {
                    selectedTool = clickedTool;
                }
            }
        }
    }

    private void activateEmpOrDdos() {
        if (bandwidth >= EMP_COST && empFreezeTimer <= 0) {
            bandwidth -= EMP_COST;
            empFreezeTimer = 6.0;
            audio.playPowerup();
            triggerScreenShake(12.0, Color.web("#38BDF8"), 0.35);
            spawnPopup(
                    scene.getWidth() / 2.0,
                    ORIGIN[1] - 25,
                    redTeamMode ? "📡 ATTAQUE DDoS (6.0s) ACTIVE !" : "❄️ EMP CRYO-FREEZE (6.0s) ACTIF !",
                    Color.web("#38BDF8"),
                    18,
                    PopupStyle.HEX_TECH
            );
            shockwaves.add(new Shockwave(ORIGIN[0] + (NBcases * LENcases) / 2.0, ORIGIN[1] + (NBcases * LENcases) / 2.0, 420, Color.web("#38BDF8")));
        } else if (bandwidth < EMP_COST) {
            spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 20, "BESOIN DE 45 ÉNERGIE !", Color.web("#FF2A6D"), 15, PopupStyle.WARNING_SHIELD);
        }
    }

    private void launchRedTeamMissileSalvo() {
        if (bandwidth < 30.0) {
            spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 20, "BESOIN DE 30 TFLOPS !", Color.web("#FF2A6D"), 15, PopupStyle.WARNING_SHIELD);
            return;
        }
        bandwidth -= 30.0;
        audio.playLaser();
        for (int i = 0; i < 3; i++) {
            launchEnemyVirusMissile();
        }
        spawnPopup(
                scene.getWidth() / 2.0,
                ORIGIN[1] - 25,
                "🚀 SALVE DE 3 MISSILES LANCÉE !",
                Color.web("#FACC15"),
                17,
                PopupStyle.SPEECH_BUBBLE
        );
    }

    private void updateHover(double mx, double my) {
        this.mouseX = mx;
        this.mouseY = my;
        hoverX = (int) Math.floor((mx - ORIGIN[0]) / LENcases);
        hoverY = (int) Math.floor((my - ORIGIN[1]) / LENcases);
    }

    private boolean tryInterceptVirusMissile(double mx, double my) {
        Iterator<VirusMissile> it = virusMissiles.iterator();
        while (it.hasNext()) {
            VirusMissile vm = it.next();
            if (Math.hypot(mx - vm.x, my - vm.y) <= 30.0) {
                it.remove();
                missilesIntercepted++;
                orbitalCharge = Math.min(100.0, orbitalCharge + 14.0);
                audio.playLaser();
                fireOrbitalLaser(vm.x, vm.y, Color.web("#FACC15"));
                spawnExplosion(vm.x, vm.y, Color.web("#FACC15"), 40, 320);
                spawnExplosion(vm.x, vm.y, Color.web("#FF2A6D"), 25, 220);
                shockwaves.add(new Shockwave(vm.x, vm.y, 85, Color.web("#FACC15")));
                triggerScreenShake(10.0, Color.web("#FACC15"), 0.25);
                addScore(250, 45, vm.x, vm.y, "🎯 INTERCEPTÉ +250 XP", Color.web("#38BDF8"), 16, PopupStyle.SPEECH_BUBBLE);

                if (missilesIntercepted == 3) {
                    awardMedal("🏅 MÉDAILLE [DÔME DE FER] : 3 MISSILES INTERCEPTÉS !", 600, 250);
                }
                return true;
            }
        }
        return false;
    }

    private void handleGridInteraction(int gx, int gy, MouseButton button, boolean isDragging) {
        if (currentPlan != Plan.GAMEPLAY || roundEnding) return;
        if (gx < 0 || gx >= NBcases || gy < 0 || gy >= NBcases) return;

        if (redTeamMode) {
            handleRedTeamGridClick(gx, gy, button, isDragging);
            return;
        }

        Cell cell = grid[gy][gx];
        CellState current = cell.getState();
        double cx = ORIGIN[0] + gx * LENcases + LENcases / 2.0;
        double cy = ORIGIN[1] + gy * LENcases + LENcases / 2.0;

        // MODE HYPER-SURGE (Murs gratuits en glissant)
        if (hyperSurgeTimer > 0 && (current == CellState.EMPTY || current == CellState.DAMAGED_FIREWALL)) {
            cell.setState(CellState.FIREWALL);
            spawnExplosion(cx, cy, Color.web("#FACC15"), 10, 120);
            score += 15;
            checkFloodFillEncirclement();
            if (isAllMalwareContainedOrPurged()) {
                triggerContainmentVictory();
            }
            return;
        }

        // Clic droit ou Outil 2 = Nuke 3x3
        if (!isDragging && (button == MouseButton.SECONDARY || selectedTool == 2)) {
            if (bandwidth >= nukeCost) {
                bandwidth -= nukeCost;
                detonateQuarantineNuke(gx, gy, cx, cy, true);
                selectedTool = 1;
            } else {
                spawnPopup(cx, cy, "BESOIN DE " + (int) nukeCost + " BW !", Color.web("#F59E0B"), 14, PopupStyle.WARNING_SHIELD);
            }
            return;
        }

        // Outil 4 : Tourelle Sentry (Niveau 3+)
        if (!isDragging && selectedTool == 4 && wave >= 3) {
            if (current == CellState.EMPTY && bandwidth >= TURRET_COST) {
                bandwidth -= TURRET_COST;
                cell.setState(CellState.TURRET);
                audio.playPowerup();
                spawnExplosion(cx, cy, Color.web("#10B981"), 28, 170);
                shockwaves.add(new Shockwave(cx, cy, LENcases * 1.9, Color.web("#10B981")));
                spawnPopup(cx, cy, "🛰️ SENTRY ACTIVÉE !", Color.web("#10B981"), 15, PopupStyle.HEX_TECH);
                selectedTool = 1;
                checkFloodFillEncirclement();
            }
            return;
        }

        // Outil 5 : Mine Plasma (Niveau 2+)
        if (!isDragging && selectedTool == 5 && wave >= 2) {
            if (current == CellState.EMPTY && bandwidth >= MINE_COST) {
                bandwidth -= MINE_COST;
                cell.setState(CellState.PLASMA_MINE);
                audio.playPowerup();
                spawnExplosion(cx, cy, Color.web("#D946EF"), 24, 150);
                shockwaves.add(new Shockwave(cx, cy, LENcases * 1.7, Color.web("#D946EF")));
                spawnPopup(cx, cy, "💣 MINE PLASMA !", Color.web("#D946EF"), 15, PopupStyle.SPEECH_BUBBLE);
                selectedTool = 1;
            }
            return;
        }

        // Outil 6 : POT DE MIEL / HONEYPOT
        if (!isDragging && selectedTool == 6) {
            if (current == CellState.EMPTY && bandwidth >= HONEYPOT_COST) {
                bandwidth -= HONEYPOT_COST;
                cell.setState(CellState.HONEYPOT);
                cell.honeypotAbsorbed = 0;
                audio.playPowerup();
                spawnExplosion(cx, cy, Color.web("#FACC15"), 26, 160);
                shockwaves.add(new Shockwave(cx, cy, LENcases * 2.2, Color.web("#FACC15")));
                spawnPopup(cx, cy, "🍯 POT DE MIEL ACTIF !", Color.web("#FACC15"), 15, PopupStyle.SPEECH_BUBBLE);
                selectedTool = 1;
                checkFloodFillEncirclement();
            }
            return;
        }

        // Outil 1 : Laser Orbital sur Virus OU Construction de Firewall
        if (current.isMalware() && bandwidth >= PURGE_COST) {
            bandwidth -= PURGE_COST;
            audio.playLaser();
            fireOrbitalLaser(cx, cy, Color.web("#05D5FA"));
            launchPlayerSwarm(cx, cy, 4, Color.web("#FACC15"));
            purgeMalwareCell(gx, gy, cx, cy);

            if (selectedDeck == 1) {
                triggerNetrunnerChainLightning(gx, gy, cx, cy);
            }

            if (isAllMalwareContainedOrPurged()) {
                triggerContainmentVictory();
            }
        } else if (current == CellState.EMPTY && bandwidth >= FIREWALL_COST) {
            cell.setState(CellState.FIREWALL);
            bandwidth -= FIREWALL_COST;
            spawnExplosion(cx, cy, Color.web("#05D5FA"), 10, 100);
            checkFloodFillEncirclement();
            if (isAllMalwareContainedOrPurged()) {
                triggerContainmentVictory();
            }
        } else if (current == CellState.DAMAGED_FIREWALL && bandwidth >= REPAIR_COST) {
            cell.setState(CellState.FIREWALL);
            bandwidth -= REPAIR_COST;
            score += 30;
            spawnExplosion(cx, cy, Color.web("#38BDF8"), 14, 120);
            checkFloodFillEncirclement();
        }
    }

    private void handleRedTeamGridClick(int gx, int gy, MouseButton button, boolean isDragging) {
        Cell cell = grid[gy][gx];
        CellState current = cell.getState();
        double cx = ORIGIN[0] + gx * LENcases + LENcases / 2.0;
        double cy = ORIGIN[1] + gy * LENcases + LENcases / 2.0;

        if (hyperSurgeTimer > 0 && (current == CellState.EMPTY || current == CellState.FIREWALL || current == CellState.DAMAGED_FIREWALL)) {
            cell.setState(CellState.VIRUS);
            spawnExplosion(cx, cy, Color.web("#FF2A6D"), 10, 120);
            score += 20;
            if (hasRedTeamEscapedOrHackedAllCores()) triggerRedTeamVictory();
            return;
        }

        if (!isDragging && (button == MouseButton.SECONDARY || selectedTool == 2)) {
            if (bandwidth >= nukeCost) {
                bandwidth -= nukeCost;
                audio.playNuke();
                triggerScreenShake(22.0, Color.web("#FF2A6D"), 0.45);
                int wallsShattered = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = gx + dx, ny = gy + dy;
                        if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                            CellState st = grid[ny][nx].getState();
                            if (st == CellState.FIREWALL || st == CellState.DAMAGED_FIREWALL || st == CellState.TURRET) {
                                grid[ny][nx].setState(CellState.EMPTY);
                                wallsShattered++;
                                killCount++;
                            }
                        }
                    }
                }
                grid[gy][gx].setState(CellState.VIRUS);
                orbitalCharge = Math.min(100.0, orbitalCharge + 15.0);
                spawnExplosion(cx, cy, Color.web("#FF2A6D"), 60, 360);
                shockwaves.add(new Shockwave(cx, cy, LENcases * 4.2, Color.web("#FF2A6D")));
                addScore(200 + wallsShattered * 120, 45, cx, cy, "💣 BOMBE 3x3 : " + wallsShattered + " MURS BRISÉS !", Color.web("#FF2A6D"), 18, PopupStyle.STARBURST);
                selectedTool = 1;
                if (hasRedTeamEscapedOrHackedAllCores()) triggerRedTeamVictory();
            } else {
                spawnPopup(cx, cy, "BESOIN DE " + (int) nukeCost + " TF !", Color.web("#F59E0B"), 14, PopupStyle.WARNING_SHIELD);
            }
            return;
        }

        if (!isDragging && selectedTool == 4) {
            if ((current == CellState.EMPTY || current == CellState.DATA_CORE) && bandwidth >= 25.0 && isWithinDistanceOfMalware(gx, gy, 3)) {
                bandwidth -= 25.0;
                cell.setState(CellState.TROJAN);
                audio.playPowerup();
                spawnExplosion(cx, cy, Color.web("#A855F7"), 28, 190);
                shockwaves.add(new Shockwave(cx, cy, LENcases * 2.0, Color.web("#A855F7")));
                addScore(150, 30, cx, cy, "🐴 SAUT TROJAN !", Color.web("#A855F7"), 16, PopupStyle.SPEECH_BUBBLE);
                selectedTool = 1;
                if (hasRedTeamEscapedOrHackedAllCores()) triggerRedTeamVictory();
            } else {
                spawnPopup(cx, cy, "CLIQUE À <= 3 CASES DE TON VIRUS !", Color.web("#A855F7"), 14, PopupStyle.WARNING_SHIELD);
            }
            return;
        }

        if (!isDragging && selectedTool == 5) {
            if (bandwidth >= 25.0 && isWithinDistanceOfMalware(gx, gy, 2)) {
                bandwidth -= 25.0;
                audio.playLaser();
                int dx = (gx < NBcases / 2) ? -1 : 1;
                int dy = (gy < NBcases / 2) ? -1 : 1;
                if (Math.abs(gx - NBcases / 2) > Math.abs(gy - NBcases / 2)) dy = 0;
                else dx = 0;

                for (int step = 0; step < 3; step++) {
                    int nx = gx + dx * step, ny = gy + dy * step;
                    if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                        grid[ny][nx].setState(CellState.WORM);
                        double wx = ORIGIN[0] + nx * LENcases + LENcases / 2.0;
                        double wy = ORIGIN[1] + ny * LENcases + LENcases / 2.0;
                        spawnExplosion(wx, wy, Color.web("#84CC16"), 16, 150);
                    }
                }
                addScore(180, 35, cx, cy, "🐛 VER SUPERSONIQUE !", Color.web("#84CC16"), 16, PopupStyle.HEX_TECH);
                selectedTool = 1;
                if (hasRedTeamEscapedOrHackedAllCores()) triggerRedTeamVictory();
            } else {
                spawnPopup(cx, cy, "CLIQUE PRÈS DE TON VIRUS !", Color.web("#84CC16"), 14, PopupStyle.WARNING_SHIELD);
            }
            return;
        }

        if ((current == CellState.EMPTY || current == CellState.DATA_CORE) && bandwidth >= 10.0 && isWithinDistanceOfMalware(gx, gy, 1)) {
            bandwidth -= 10.0;
            boolean wasCore = (current == CellState.DATA_CORE);
            cell.setState(CellState.VIRUS);
            orbitalCharge = Math.min(100.0, orbitalCharge + 6.0);
            spawnExplosion(cx, cy, Color.web("#FF2A6D"), 12, 120);
            if (wasCore) {
                audio.playNuke();
                addScore(400, 100, cx, cy, "💎 SERVEUR PIRATÉ +400 XP !", Color.web("#FACC15"), 18, PopupStyle.STARBURST);
            }
            if (hasRedTeamEscapedOrHackedAllCores()) triggerRedTeamVictory();
        } else if ((current == CellState.FIREWALL || current == CellState.DAMAGED_FIREWALL || current == CellState.TURRET) && bandwidth >= 12.0) {
            bandwidth -= 12.0;
            audio.playExplosion();
            fireOrbitalLaser(cx, cy, Color.web("#FF2A6D"));
            if (current == CellState.FIREWALL) {
                cell.setState(CellState.DAMAGED_FIREWALL);
            } else {
                cell.setState(CellState.VIRUS);
                killCount++;
                orbitalCharge = Math.min(100.0, orbitalCharge + 10.0);
                spawnExplosion(cx, cy, Color.web("#FF2A6D"), 28, 220);
                addScore(150, 30, cx, cy, "☣️ DÉFENSE IA CORROMPUE !", Color.web("#FF2A6D"), 16, PopupStyle.STARBURST);
                if (hasRedTeamEscapedOrHackedAllCores()) triggerRedTeamVictory();
            }
        }
    }

    private boolean isWithinDistanceOfMalware(int gx, int gy, int maxDist) {
        for (int y = Math.max(0, gy - maxDist); y <= Math.min(NBcases - 1, gy + maxDist); y++) {
            for (int x = Math.max(0, gx - maxDist); x <= Math.min(NBcases - 1, gx + maxDist); x++) {
                if (grid[y][x].getState().isMalware()) {
                    if (Math.abs(x - gx) + Math.abs(y - gy) <= maxDist) return true;
                }
            }
        }
        return false;
    }

    private void triggerNetrunnerChainLightning(int startGx, int startGy, double startCx, double startCy) {
        int chainsLeft = 2;
        double prevX = startCx, prevY = startCy;

        for (int r = 1; r <= 4 && chainsLeft > 0; r++) {
            for (int y = Math.max(0, startGy - r); y <= Math.min(NBcases - 1, startGy + r) && chainsLeft > 0; y++) {
                for (int x = Math.max(0, startGx - r); x <= Math.min(NBcases - 1, startGx + r) && chainsLeft > 0; x++) {
                    CellState st = grid[y][x].getState();
                    if (st.isMalware()) {
                        double tx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                        double ty = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                        lasers.add(new CyberLaser(prevX, prevY, tx, ty, Color.web("#FACC15"), 4.0));
                        purgeMalwareCell(x, y, tx, ty);
                        prevX = tx;
                        prevY = ty;
                        chainsLeft--;
                    }
                }
            }
        }
    }

    private void detonatePlasmaMine(int mineX, int mineY) {
        double cx = ORIGIN[0] + mineX * LENcases + LENcases / 2.0;
        double cy = ORIGIN[1] + mineY * LENcases + LENcases / 2.0;
        grid[mineY][mineX].setState(CellState.EMPTY);

        audio.playNuke();
        triggerScreenShake(16.0, Color.web("#D946EF"), 0.35);
        lasers.add(new CyberLaser(ORIGIN[0] + Math.max(0, mineX - 3) * LENcases, cy, ORIGIN[0] + Math.min(NBcases, mineX + 4) * LENcases, cy, Color.web("#D946EF"), 7.0));
        lasers.add(new CyberLaser(cx, ORIGIN[1] + Math.max(0, mineY - 3) * LENcases, cx, ORIGIN[1] + Math.min(NBcases, mineY + 4) * LENcases, Color.web("#D946EF"), 7.0));

        int killedByMine = 0;
        for (int d = -3; d <= 3; d++) {
            int[][] crossCoords = {{mineX + d, mineY}, {mineX, mineY + d}};
            for (int[] pt : crossCoords) {
                int nx = pt[0], ny = pt[1];
                if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                    CellState st = grid[ny][nx].getState();
                    double tx = ORIGIN[0] + nx * LENcases + LENcases / 2.0;
                    double ty = ORIGIN[1] + ny * LENcases + LENcases / 2.0;
                    if (st.isMalware()) {
                        grid[ny][nx].setState(CellState.EMPTY);
                        killedByMine++;
                        killCount++;
                        waveKills++;
                        spawnExplosion(tx, ty, Color.web("#D946EF"), 26, 260);
                    }
                }
            }
        }

        shockwaves.add(new Shockwave(cx, cy, LENcases * 3.8, Color.web("#D946EF")));
        int bonus = 250 + killedByMine * 140;
        addScore(bonus, 50, cx, cy, "💥 CROIX PLASMA +" + bonus + " XP !", Color.web("#D946EF"), 18, PopupStyle.STARBURST);
    }

    private void launchPlayerSwarm(double targetX, double targetY, int count, Color color) {
        double startX = ORIGIN[0] + (NBcases * LENcases) / 2.0;
        double startY = ORIGIN[1] + NBcases * LENcases + 20;
        for (int i = 0; i < count; i++) {
            double spreadX = targetX + (random.nextDouble() - 0.5) * LENcases * 2.2;
            double spreadY = targetY + (random.nextDouble() - 0.5) * LENcases * 2.2;
            double curve = (random.nextDouble() - 0.5) * 260.0;
            swarmProjectiles.add(new SwarmProjectile(startX + (i - count / 2.0) * 18, startY, spreadX, spreadY, curve, color));
        }
    }

    private void launchEnemyVirusMissile() {
        List<int[]> infected = new ArrayList<>();
        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                if (grid[y][x].getState().isMalware()) infected.add(new int[]{x, y});
            }
        }
        if (infected.isEmpty()) return;
        int[] shooter = infected.get(random.nextInt(infected.size()));
        double startX = ORIGIN[0] + shooter[0] * LENcases + LENcases / 2.0;
        double startY = ORIGIN[1] + shooter[1] * LENcases + LENcases / 2.0;

        int targetGx = random.nextInt(1, NBcases - 1);
        int targetGy = random.nextInt(1, NBcases - 1);

        boolean honeypotFound = false;
        if (!redTeamMode) {
            for (int y = 0; y < NBcases && !honeypotFound; y++) {
                for (int x = 0; x < NBcases; x++) {
                    if (grid[y][x].getState() == CellState.HONEYPOT) {
                        targetGx = x;
                        targetGy = y;
                        honeypotFound = true;
                        break;
                    }
                }
            }
        }

        if (!honeypotFound) {
            for (int tries = 0; tries < 15; tries++) {
                int rx = random.nextInt(1, NBcases - 1);
                int ry = random.nextInt(1, NBcases - 1);
                if (grid[ry][rx].getState() == CellState.FIREWALL || grid[ry][rx].getState() == CellState.TURRET || grid[ry][rx].getState() == CellState.DATA_CORE) {
                    targetGx = rx;
                    targetGy = ry;
                    break;
                }
            }
        }

        double speed = (redTeamMode ? 185.0 : 135.0) + wave * 18.0;
        virusMissiles.add(new VirusMissile(startX, startY, targetGx, targetGy, speed));
    }

    private void updateMissilesAndSwarms(double dt) {
        Iterator<VirusMissile> vmIt = virusMissiles.iterator();
        while (vmIt.hasNext()) {
            VirusMissile vm = vmIt.next();
            double targetX = ORIGIN[0] + vm.targetGx * LENcases + LENcases / 2.0;
            double targetY = ORIGIN[1] + vm.targetGy * LENcases + LENcases / 2.0;
            vm.update(dt, targetX, targetY);

            if (random.nextDouble() < 0.65) {
                particles.add(new Particle(vm.x, vm.y, (random.nextDouble() - 0.5) * 35, (random.nextDouble() - 0.5) * 35, 4.5, Color.web("#FF2A6D"), false));
            }

            if (vm.reachedTarget) {
                vmIt.remove();
                audio.playExplosion();
                Cell targetCell = grid[vm.targetGy][vm.targetGx];
                CellState current = targetCell.getState();

                if (current == CellState.HONEYPOT && !redTeamMode) {
                    targetCell.honeypotAbsorbed++;
                    if (targetCell.honeypotAbsorbed >= 3) {
                        triggerHoneypotRetroEngineering(vm.targetGx, vm.targetGy);
                    }
                } else if (current == CellState.FIREWALL && !redTeamMode) {
                    targetCell.setState(CellState.DAMAGED_FIREWALL);
                } else if (current == CellState.PLASMA_MINE) {
                    detonatePlasmaMine(vm.targetGx, vm.targetGy);
                } else {
                    targetCell.setState(CellState.VIRUS);
                    if (redTeamMode && hasRedTeamEscapedOrHackedAllCores()) {
                        triggerRedTeamVictory();
                    }
                }
                spawnExplosion(targetX, targetY, Color.web("#FF2A6D"), 32, 240);
                shockwaves.add(new Shockwave(targetX, targetY, LENcases * 2.0, Color.web("#FF2A6D")));
                triggerScreenShake(9.0, Color.web("#FF2A6D"), 0.2);
            }
        }

        Iterator<SwarmProjectile> spIt = swarmProjectiles.iterator();
        while (spIt.hasNext()) {
            SwarmProjectile sp = spIt.next();
            sp.update(dt);
            if (random.nextDouble() < 0.7) {
                particles.add(new Particle(sp.x, sp.y, 0, 0, 3.5, sp.color, true));
            }
            if (sp.progress >= 1.0) {
                spIt.remove();
                spawnExplosion(sp.targetX, sp.targetY, sp.color, 10, 140);
                shockwaves.add(new Shockwave(sp.targetX, sp.targetY, LENcases * 1.1, sp.color));
            }
        }
    }

    private void detonateQuarantineNuke(int centerGx, int centerGy, double cx, double cy, boolean allowPortalJump) {
        audio.playNuke();
        lasers.add(new CyberLaser(scene.getWidth() / 2.0, 0, cx, cy, Color.web("#FACC15"), 9.0));
        launchPlayerSwarm(cx, cy, 12, Color.web("#FACC15"));
        triggerScreenShake(24.0, Color.web("#FACC15"), 0.48);

        int purgedInNuke = 0;
        boolean touchedPortalA = false;
        boolean touchedPortalB = false;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = centerGx + dx;
                int ny = centerGy + dy;
                if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                    CellState st = grid[ny][nx].getState();
                    double cellX = ORIGIN[0] + nx * LENcases + LENcases / 2.0;
                    double cellY = ORIGIN[1] + ny * LENcases + LENcases / 2.0;

                    if (st == CellState.PORTAL_A) touchedPortalA = true;
                    if (st == CellState.PORTAL_B) touchedPortalB = true;

                    if (st.isMalware()) {
                        grid[ny][nx].setState(CellState.EMPTY);
                        purgedInNuke++;
                        killCount++;
                        waveKills++;
                        orbitalCharge = Math.min(100.0, orbitalCharge + 8.0);
                        spawnExplosion(cellX, cellY, st.getBaseColor(), 28, 290);
                    }
                }
            }
        }

        spawnExplosion(cx, cy, Color.web("#FACC15"), 70, 400);
        spawnExplosion(cx, cy, Color.web("#FF2A6D"), 50, 310);
        shockwaves.add(new Shockwave(cx, cy, LENcases * 4.8, Color.web("#FACC15")));
        shockwaves.add(new Shockwave(cx, cy, LENcases * 3.0, Color.web("#FF2A6D")));

        int nukeBonus = Math.max(200, purgedInNuke * 200);
        addScore(nukeBonus, 45, cx, cy, "☢️ NUKE +" + nukeBonus + " XP !", Color.web("#FACC15"), 18, PopupStyle.STARBURST);

        if (allowPortalJump && portalsUnlocked) {
            if (touchedPortalA) {
                double pbx = ORIGIN[0] + portalBX * LENcases + LENcases / 2.0;
                double pby = ORIGIN[1] + portalBY * LENcases + LENcases / 2.0;
                lasers.add(new CyberLaser(cx, cy, pbx, pby, Color.web("#0EA5E9"), 7.0));
                detonateQuarantineNuke(portalBX, portalBY, pbx, pby, false);
            } else if (touchedPortalB) {
                double pax = ORIGIN[0] + portalAX * LENcases + LENcases / 2.0;
                double pay = ORIGIN[1] + portalAY * LENcases + LENcases / 2.0;
                lasers.add(new CyberLaser(cx, cy, pax, pay, Color.web("#F97316"), 7.0));
                detonateQuarantineNuke(portalAX, portalAY, pax, pay, false);
            }
        }

        if (isAllMalwareContainedOrPurged()) {
            triggerContainmentVictory();
        }
    }

    private void fireOrbitalLaser(double targetX, double targetY, Color color) {
        double cannonX = ORIGIN[0] + (NBcases * LENcases) / 2.0;
        double cannonY = ORIGIN[1] - 12;
        lasers.add(new CyberLaser(cannonX, cannonY, targetX, targetY, color, 4.5));
        audioEnergy = Math.min(1.0, audioEnergy + 0.45);
    }

    private void purgeMalwareCell(int gx, int gy, double cx, double cy) {
        CellState killedStrain = grid[gy][gx].getState();
        if (!killedStrain.isMalware()) return;

        grid[gy][gx].setState(CellState.EMPTY);
        if (redTeamMode) {
            spawnExplosion(cx, cy, Color.web("#05D5FA"), 20, 180);
            return;
        }

        killCount++;
        waveKills++;
        orbitalCharge = Math.min(100.0, orbitalCharge + 9.0);

        combo++;
        comboTimer = 4.0;
        audio.playExplosion();
        triggerScreenShake(8.0, killedStrain.getBaseColor(), 0.15);

        if (combo == 3 || killCount % 5 == 0) {
            doubleXpTimer = 8.0;
            audio.playPowerup();
            spawnPopup(scene.getWidth() / 2.0, ORIGIN[1] - 25, "⚡ OVERDRIVE 2X XP ! ⚡", Color.web("#FACC15"), 18, PopupStyle.HEX_TECH);
        }

        if (waveKills == 15) {
            awardMedal("🔥 MÉDAILLE [EXTERMINATEUR] : 15 VIRUS PURGÉS !", 700, 220);
        }

        int strainBase = switch (killedStrain) {
            case RANSOMWARE -> 350;
            case TROJAN -> 200;
            case WORM -> 160;
            default -> 110;
        };

        int points = strainBase * Math.min(6, combo);
        if (blackoutTimer > 0) points = (int) (points * 1.5);
        int creditDrop = (killedStrain == CellState.RANSOMWARE) ? 85 : 22;

        PopupStyle chosenStyle;
        int fontSize;
        Color popupColor;

        if (combo >= 3 || killedStrain == CellState.RANSOMWARE) {
            chosenStyle = PopupStyle.STARBURST;
            fontSize = 15 + Math.min(5, combo);
            popupColor = Color.web("#FACC15");
        } else if (killedStrain == CellState.TROJAN || killedStrain == CellState.WORM) {
            chosenStyle = PopupStyle.SPEECH_BUBBLE;
            fontSize = 15;
            popupColor = killedStrain.getBaseColor();
        } else {
            chosenStyle = comicMode ? PopupStyle.SPEECH_BUBBLE : PopupStyle.HEX_TECH;
            fontSize = 14;
            popupColor = Color.web("#FF2A6D");
        }

        String tag = pickActionWord() + " +" + points + " XP";
        addScore(points, creditDrop, cx, cy, tag, popupColor, fontSize, chosenStyle);

        spawnExplosion(cx, cy, killedStrain.getBaseColor(), 38, 270);
        spawnExplosion(cx, cy, Color.web("#FACC15"), 16, 190);
        shockwaves.add(new Shockwave(cx, cy, LENcases * 2.3, killedStrain.getBaseColor()));
        shockwaves.add(new Shockwave(cx, cy, LENcases * 1.4, Color.WHITE));
    }

    private void addScore(int basePoints, int creditGain, double x, double y, String text, Color color, int fontSize, PopupStyle style) {
        int finalPoints = (doubleXpTimer > 0) ? basePoints * 2 : basePoints;
        score += finalPoints;
        credits += creditGain;

        String display = (doubleXpTimer > 0) ? text + " [2X!]" : text;
        spawnPopup(x, y, display, color, fontSize, style);

        for (int r = RANK_THRESHOLDS.length - 1; r > currentRankIndex; r--) {
            if (score >= RANK_THRESHOLDS[r]) {
                currentRankIndex = r;
                audio.playPowerup();
                awardMedal("🎖️ PROMOTION : " + RANK_NAMES[currentRankIndex] + " !", 500, 200);
                break;
            }
        }
    }

    private void awardMedal(String title, int bonusXp, int bonusCredits) {
        totalMedalsEarned++;
        score += bonusXp;
        credits += bonusCredits;
        activeMedalBanner = title + " (+" + bonusXp + " XP | +" + bonusCredits + " $)";
        medalBannerTimer = 4.5;
        audio.playPowerup();
    }

    private void updateGridTurretsTeslaAndMines(double dt) {
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        List<int[]> turretCoords = new ArrayList<>();

        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                Cell cell = grid[y][x];
                cell.updateAnimation(dt);

                if (cell.getState() == CellState.TURRET && !roundEnding && (!redTeamMode || empFreezeTimer <= 0)) {
                    turretCoords.add(new int[]{x, y});
                    cell.turretCooldown -= dt;
                    if (cell.turretCooldown <= 0) {
                        if (tryTurretFire(x, y)) {
                            cell.turretCooldown = turretFireInterval;
                        } else {
                            cell.turretCooldown = 0.25;
                        }
                    }
                } else if (cell.getState() == CellState.PLASMA_MINE && !roundEnding) {
                    for (int[] d : dirs) {
                        int nx = x + d[0], ny = y + d[1];
                        if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                            if (grid[ny][nx].getState().isMalware()) {
                                detonatePlasmaMine(x, y);
                                break;
                            }
                        }
                    }
                }
            }
        }

        activeTeslaLinks.clear();
        teslaZapTimer -= dt;
        boolean triggerZap = (teslaZapTimer <= 0);
        if (triggerZap) teslaZapTimer = 0.35;

        for (int i = 0; i < turretCoords.size(); i++) {
            for (int j = i + 1; j < turretCoords.size(); j++) {
                int[] t1 = turretCoords.get(i);
                int[] t2 = turretCoords.get(j);

                if (t1[0] == t2[0] && Math.abs(t1[1] - t2[1]) <= 6) {
                    activeTeslaLinks.add(new int[]{t1[0], t1[1], t2[0], t2[1]});
                    if (triggerZap) {
                        int minY = Math.min(t1[1], t2[1]) + 1;
                        int maxY = Math.max(t1[1], t2[1]) - 1;
                        for (int y = minY; y <= maxY; y++) zapCellWithTeslaBarrier(t1[0], y);
                    }
                } else if (t1[1] == t2[1] && Math.abs(t1[0] - t2[0]) <= 6) {
                    activeTeslaLinks.add(new int[]{t1[0], t1[1], t2[0], t2[1]});
                    if (triggerZap) {
                        int minX = Math.min(t1[0], t2[0]) + 1;
                        int maxX = Math.max(t1[0], t2[0]) - 1;
                        for (int x = minX; x <= maxX; x++) zapCellWithTeslaBarrier(x, t1[1]);
                    }
                }
            }
        }
    }

    private void zapCellWithTeslaBarrier(int gx, int gy) {
        CellState st = grid[gy][gx].getState();
        double cx = ORIGIN[0] + gx * LENcases + LENcases / 2.0;
        double cy = ORIGIN[1] + gy * LENcases + LENcases / 2.0;
        if (st.isMalware()) {
            purgeMalwareCell(gx, gy, cx, cy);
        }
    }

    private void updateDrones(double dt) {
        double centerPx = ORIGIN[0] + (NBcases * LENcases) / 2.0;
        double centerPy = ORIGIN[1] + (NBcases * LENcases) / 2.0;
        double orbitRadius = NBcases * LENcases * 0.34;

        for (AllyDrone d : drones) {
            d.update(dt, centerPx, centerPy, orbitRadius);
            if (d.actionCooldown <= 0 && !roundEnding) {
                if (redTeamMode) {
                    for (int y = 0; y < NBcases; y++) {
                        boolean acted = false;
                        for (int x = 0; x < NBcases; x++) {
                            if (grid[y][x].getState() == CellState.FIREWALL || grid[y][x].getState() == CellState.DAMAGED_FIREWALL) {
                                double tx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                                double ty = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                                if (Math.hypot(tx - d.x, ty - d.y) <= LENcases * 6.5) {
                                    grid[y][x].setState(CellState.EMPTY);
                                    lasers.add(new CyberLaser(d.x, d.y, tx, ty, Color.web("#FF2A6D"), 3.2));
                                    spawnExplosion(tx, ty, Color.web("#FF2A6D"), 15, 130);
                                    d.actionCooldown = 1.4;
                                    acted = true;
                                    break;
                                }
                            }
                        }
                        if (acted) break;
                    }
                } else if (d.isHunter) {
                    for (int y = 0; y < NBcases; y++) {
                        boolean fired = false;
                        for (int x = 0; x < NBcases; x++) {
                            CellState st = grid[y][x].getState();
                            if (st.isMalware()) {
                                double tx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                                double ty = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                                if (Math.hypot(tx - d.x, ty - d.y) <= LENcases * 6.5) {
                                    lasers.add(new CyberLaser(d.x, d.y, tx, ty, Color.web("#FACC15"), 3.2));
                                    purgeMalwareCell(x, y, tx, ty);
                                    d.actionCooldown = 1.35;
                                    fired = true;
                                    break;
                                }
                            }
                        }
                        if (fired) break;
                    }
                } else {
                    for (int y = 0; y < NBcases; y++) {
                        boolean repaired = false;
                        for (int x = 0; x < NBcases; x++) {
                            if (grid[y][x].getState() == CellState.DAMAGED_FIREWALL) {
                                double tx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                                double ty = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                                grid[y][x].setState(CellState.FIREWALL);
                                lasers.add(new CyberLaser(d.x, d.y, tx, ty, Color.web("#05D5FA"), 3.0));
                                spawnExplosion(tx, ty, Color.web("#05D5FA"), 12, 100);
                                d.actionCooldown = 1.6;
                                repaired = true;
                                break;
                            }
                        }
                        if (repaired) break;
                    }
                }
            }
        }
    }

    private boolean tryTurretFire(int tx, int ty) {
        double turretX = ORIGIN[0] + tx * LENcases + LENcases / 2.0;
        double turretY = ORIGIN[1] + ty * LENcases + LENcases / 2.0;

        if (!redTeamMode) {
            Iterator<VirusMissile> vmIt = virusMissiles.iterator();
            while (vmIt.hasNext()) {
                VirusMissile vm = vmIt.next();
                if (Math.hypot(vm.x - turretX, vm.y - turretY) <= LENcases * 6.5) {
                    vmIt.remove();
                    missilesIntercepted++;
                    audio.playLaser();
                    lasers.add(new CyberLaser(turretX, turretY, vm.x, vm.y, Color.web("#10B981"), 4.0));
                    spawnExplosion(vm.x, vm.y, Color.web("#10B981"), 30, 240);
                    shockwaves.add(new Shockwave(vm.x, vm.y, 70, Color.web("#10B981")));
                    addScore(200, 35, vm.x, vm.y, "🛰️ SENTRY +200 XP", Color.web("#10B981"), 15, PopupStyle.HEX_TECH);
                    return true;
                }
            }
        }

        int bestX = -1, bestY = -1;
        double bestDist = 6.0;

        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                CellState st = grid[y][x].getState();
                if (st.isMalware()) {
                    double dist = Math.hypot(x - tx, y - ty);
                    if (dist <= bestDist) {
                        bestDist = dist;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }

        if (bestX != -1) {
            double targetX = ORIGIN[0] + bestX * LENcases + LENcases / 2.0;
            double targetY = ORIGIN[1] + bestY * LENcases + LENcases / 2.0;
            audio.playLaser();
            lasers.add(new CyberLaser(turretX, turretY, targetX, targetY, Color.web("#10B981"), 3.5));
            swarmProjectiles.add(new SwarmProjectile(turretX, turretY, targetX, targetY, 80, Color.web("#10B981")));

            purgeMalwareCell(bestX, bestY, targetX, targetY);
            if (!redTeamMode && isAllMalwareContainedOrPurged()) {
                triggerContainmentVictory();
            }
            return true;
        }
        return false;
    }

    private void triggerScreenShake(double intensity, Color flashColor, double flashAlpha) {
        this.shakeIntensity = Math.max(this.shakeIntensity, intensity);
        this.screenFlashColor = flashColor;
        this.screenFlashAlpha = Math.max(this.screenFlashAlpha, flashAlpha);
        this.audioEnergy = 1.0;
    }

    private void triggerContainmentVictory() {
        if (roundEnding) return;
        roundEnding = true;
        victoryDelayTimer = 2.0;
        virusMissiles.clear();
        audio.playPowerup();
        triggerScreenShake(26.0, Color.web("#4ADE80"), 0.5);

        int wipedOut = 0;
        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                if (grid[y][x].getState().isMalware()) {
                    wipedOut++;
                    double cx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                    double cy = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                    Color c = grid[y][x].getState().getBaseColor();
                    grid[y][x].setState(CellState.EMPTY);
                    killCount++;
                    spawnExplosion(cx, cy, c, 34, 310);
                    shockwaves.add(new Shockwave(cx, cy, LENcases * 2.6, Color.web("#05D5FA")));
                }
            }
        }

        int survivingCores = countCells(CellState.DATA_CORE);
        if (survivingCores == 3) {
            awardMedal("💎 MÉDAILLE [ARCHITECTE RÉSEAU] : 3 SERVEURS DATA_CORE INTACTS !", 900, 350);
        }

        int bonusXp = 600 + wipedOut * 160 + survivingCores * 350;
        int bonusCredits = 220 + survivingCores * 90;
        score += (doubleXpTimer > 0) ? bonusXp * 2 : bonusXp;
        credits += bonusCredits;

        spawnPopup(
                scene.getWidth() / 2.0,
                ORIGIN[1] - 25,
                "💥 NIVEAU SÉCURISÉ : +" + bonusXp + " XP & +" + bonusCredits + " $ !",
                Color.web("#4ADE80"),
                20,
                PopupStyle.STARBURST
        );
    }

    public void spawnExplosion(double x, double y, Color color, int count, double maxSpeed) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 40 + random.nextDouble() * maxSpeed;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double size = 4.0 + random.nextDouble() * 7.0;
            boolean isSquare = random.nextBoolean();
            particles.add(new Particle(x, y, vx, vy, size, color, isSquare));
        }
    }

    // DÉPORTE AUTOMATIQUEMENT LES POP-UPS HORS DE LA GRILLE POUR NE JAMAIS CACHER LE JEU !
    public void spawnPopup(double x, double y, String text, Color color, int fontSize, PopupStyle style) {
        double gridLeft = ORIGIN[0];
        double gridRight = ORIGIN[0] + NBcases * LENcases;
        double gridTop = ORIGIN[1];
        double gridBottom = ORIGIN[1] + NBcases * LENcases;

        double safeX = x;
        double safeY = y;

        // Si le pop-up devait apparaître sur la grille, on le place proprement dans les marges à gauche ou à droite !
        if (x > gridLeft - 70 && x < gridRight + 70 && y > gridTop - 15 && y < gridBottom + 15) {
            popupSideToggle++;
            if (popupSideToggle % 2 == 0) {
                safeX = Math.max(135, gridLeft - 140);
            } else {
                safeX = Math.min(scene.getWidth() - 135, gridRight + 140);
            }
            // Évite que plusieurs pop-ups se superposent exactement au même endroit dans la marge
            safeY = Math.max(gridTop + 50, Math.min(gridBottom - 40, y + ((popupSideToggle % 5) - 2) * 26));
        }

        while (floatingTexts.size() > 8) {
            floatingTexts.remove(0);
        }
        floatingTexts.add(new FloatingText(safeX, safeY, text, color, fontSize, style));
    }

    private void updateVisualEffects(double dt) {
        Iterator<Particle> pIt = particles.iterator();
        while (pIt.hasNext()) {
            Particle p = pIt.next();
            p.update(dt);
            if (p.life <= 0) pIt.remove();
        }

        Iterator<Shockwave> sIt = shockwaves.iterator();
        while (sIt.hasNext()) {
            Shockwave s = sIt.next();
            s.update(dt);
            if (s.life <= 0) sIt.remove();
        }

        Iterator<CyberLaser> lIt = lasers.iterator();
        while (lIt.hasNext()) {
            CyberLaser l = lIt.next();
            l.update(dt);
            if (l.life <= 0) lIt.remove();
        }

        Iterator<FloatingText> fIt = floatingTexts.iterator();
        while (fIt.hasNext()) {
            FloatingText ft = fIt.next();
            ft.update(dt);
            if (ft.life <= 0) fIt.remove();
        }
    }

    // PROPAGATION BEAUCOUP PLUS AGRESSIVE ET RAPIDE SELON LE NIVEAU !
    public void spreadMalware() {
        CellState[][] nextStates = new CellState[NBcases][NBcases];
        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                nextStates[y][x] = grid[y][x].getState();
            }
        }

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        double firewallErosionChance = redTeamMode ? 0.30 : Math.min(0.45, (selectedDeck == 0 ? 0.14 : 0.22) + wave * 0.04);

        List<int[]> honeypots = new ArrayList<>();
        if (!redTeamMode) {
            for (int y = 0; y < NBcases; y++) {
                for (int x = 0; x < NBcases; x++) {
                    if (grid[y][x].getState() == CellState.HONEYPOT) {
                        honeypots.add(new int[]{x, y});
                    }
                }
            }
        }

        List<int[]> triggeredHoneypots = new ArrayList<>();

        // À partir du Niveau 2, chaque cellule virale tente d'infecter plusieurs directions par tick !
        int attemptsPerCell = (!redTeamMode && wave >= 3) ? 2 : 1;

        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                CellState current = grid[y][x].getState();
                if (!current.isMalware()) continue;

                for (int attempt = 0; attempt < attemptsPerCell; attempt++) {
                    int[] dir = directions[random.nextInt(directions.length)];

                    if (!honeypots.isEmpty() && random.nextDouble() < 0.70) {
                        int[] bestHp = null;
                        double bestDist = 7.5;
                        for (int[] hp : honeypots) {
                            double d = Math.hypot(hp[0] - x, hp[1] - y);
                            if (d < bestDist) {
                                bestDist = d;
                                bestHp = hp;
                            }
                        }
                        if (bestHp != null) {
                            int dx = Integer.compare(bestHp[0], x);
                            int dy = Integer.compare(bestHp[1], y);
                            if (dx != 0 && dy != 0) {
                                dir = random.nextBoolean() ? new int[]{dx, 0} : new int[]{0, dy};
                            } else if (dx != 0) {
                                dir = new int[]{dx, 0};
                            } else if (dy != 0) {
                                dir = new int[]{0, dy};
                            }
                        }
                    }

                    int nx = x + dir[0];
                    int ny = y + dir[1];

                    if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                        CellState neighbor = grid[ny][nx].getState();

                        if (neighbor == CellState.HONEYPOT && !redTeamMode) {
                            grid[ny][nx].honeypotAbsorbed++;
                            double hpx = ORIGIN[0] + nx * LENcases + LENcases / 2.0;
                            double hpy = ORIGIN[1] + ny * LENcases + LENcases / 2.0;
                            shockwaves.add(new Shockwave(hpx, hpy, LENcases * 1.5, Color.web("#FACC15")));

                            if (grid[ny][nx].honeypotAbsorbed >= 3) {
                                triggeredHoneypots.add(new int[]{nx, ny});
                            }
                            continue;
                        }

                        if (portalsUnlocked && neighbor == CellState.PORTAL_A) {
                            infectAroundPortal(nextStates, portalBX, portalBY, current);
                            continue;
                        } else if (portalsUnlocked && neighbor == CellState.PORTAL_B) {
                            infectAroundPortal(nextStates, portalAX, portalAY, current);
                            continue;
                        }

                        if (neighbor == CellState.EMPTY || neighbor == CellState.DATA_CORE) {
                            if (neighbor == CellState.DATA_CORE) {
                                double cx = ORIGIN[0] + nx * LENcases + LENcases / 2.0;
                                double cy = ORIGIN[1] + ny * LENcases + LENcases / 2.0;
                                if (redTeamMode) {
                                    addScore(400, 100, cx, cy, "💎 SERVEUR PIRATÉ +400 XP", Color.web("#FACC15"), 18, PopupStyle.STARBURST);
                                } else {
                                    spawnPopup(cx, cy, "⚠️ SERVEUR TOUCHÉ ! [R] ROLLBACK !", Color.web("#FF2A6D"), 16, PopupStyle.WARNING_SHIELD);
                                }
                                audio.playExplosion();
                            }
                            nextStates[ny][nx] = (wave >= 2 && random.nextDouble() < 0.22) ? pickRandomStrain() : current;

                            if (current == CellState.WORM) {
                                int wx = nx + dir[0];
                                int wy = ny + dir[1];
                                if (wx >= 0 && wx < NBcases && wy >= 0 && wy < NBcases && grid[wy][wx].getState() == CellState.EMPTY) {
                                    nextStates[wy][wx] = CellState.WORM;
                                }
                            }
                        } else if (neighbor == CellState.FIREWALL) {
                            int jumpX = x + dir[0] * 2;
                            int jumpY = y + dir[1] * 2;
                            if (current == CellState.TROJAN && jumpX >= 0 && jumpX < NBcases && jumpY >= 0 && jumpY < NBcases
                                    && grid[jumpY][jumpX].getState() == CellState.EMPTY && random.nextDouble() < 0.38) {
                                nextStates[jumpY][jumpX] = CellState.TROJAN;
                                double jx = ORIGIN[0] + jumpX * LENcases + LENcases / 2.0;
                                double jy = ORIGIN[1] + jumpY * LENcases + LENcases / 2.0;
                                spawnPopup(jx, jy, "SAUT TROJAN !", Color.web("#A855F7"), 14, PopupStyle.SPEECH_BUBBLE);
                            } else if (random.nextDouble() < firewallErosionChance) {
                                nextStates[ny][nx] = CellState.DAMAGED_FIREWALL;
                            }
                        } else if (neighbor == CellState.DAMAGED_FIREWALL && random.nextDouble() < firewallErosionChance + 0.12) {
                            nextStates[ny][nx] = current;
                        }
                    }
                }
            }
        }

        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                grid[y][x].setState(nextStates[y][x]);
            }
        }

        for (int[] hp : triggeredHoneypots) {
            triggerHoneypotRetroEngineering(hp[0], hp[1]);
        }
    }

    private void infectAroundPortal(CellState[][] nextStates, int exitX, int exitY, CellState strain) {
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        int[] d = dirs[random.nextInt(dirs.length)];
        int tx = exitX + d[0];
        int ty = exitY + d[1];
        if (tx >= 1 && tx < NBcases - 1 && ty >= 1 && ty < NBcases - 1 && grid[ty][tx].getState() == CellState.EMPTY) {
            nextStates[ty][tx] = strain;
            double px = ORIGIN[0] + tx * LENcases + LENcases / 2.0;
            double py = ORIGIN[1] + ty * LENcases + LENcases / 2.0;
            spawnPopup(px, py, "🌀 SAUT PORTAIL !", Color.web("#F97316"), 14, PopupStyle.SPEECH_BUBBLE);
        }
    }

    private CellState pickRandomStrain() {
        double r = random.nextDouble();
        if (wave == 1) return (r < 0.80) ? CellState.VIRUS : CellState.TROJAN;
        if (wave == 2) return (r < 0.55) ? CellState.VIRUS : (r < 0.85 ? CellState.TROJAN : CellState.WORM);
        if (r < 0.45) return CellState.VIRUS;
        if (r < 0.70) return CellState.TROJAN;
        if (r < 0.88) return CellState.WORM;
        return CellState.RANSOMWARE;
    }

    public boolean checkPerimeterBreach() {
        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                if (x == 0 || x == NBcases - 1 || y == 0 || y == NBcases - 1) {
                    if (grid[y][x].getState().isMalware()) {
                        if (perimeterShields > 0) {
                            perimeterShields--;
                            grid[y][x].setState(CellState.FIREWALL);
                            double cx = ORIGIN[0] + x * LENcases + LENcases / 2.0;
                            double cy = ORIGIN[1] + y * LENcases + LENcases / 2.0;
                            audio.playNuke();
                            triggerScreenShake(20.0, Color.web("#05D5FA"), 0.45);
                            spawnExplosion(cx, cy, Color.web("#05D5FA"), 45, 300);
                            shockwaves.add(new Shockwave(cx, cy, LENcases * 3.5, Color.web("#05D5FA")));
                            spawnPopup(
                                    scene.getWidth() / 2.0,
                                    ORIGIN[1] - 25,
                                    "🛡️ BOUCLIER PÉRIMÉTRIQUE ACTIVÉ (" + perimeterShields + " RESTANTS) !",
                                    Color.web("#05D5FA"),
                                    18,
                                    PopupStyle.WARNING_SHIELD
                            );
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isAllMalwareContainedOrPurged() {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                CellState st = grid[y][x].getState();
                if (st.isMalware()) {
                    for (int[] dir : directions) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];
                        if (nx >= 0 && nx < NBcases && ny >= 0 && ny < NBcases) {
                            CellState neighbor = grid[ny][nx].getState();
                            if (neighbor == CellState.EMPTY || neighbor == CellState.DAMAGED_FIREWALL
                                    || neighbor == CellState.DATA_CORE || neighbor == CellState.PORTAL_A || neighbor == CellState.PORTAL_B) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    // =========================================================================
    // RENDU GRAPHIQUE ALTERNANT : FUTURISTE (Impair) vs BANDE DESSINÉE (Pair)
    // =========================================================================

    private void renderGame() {
        double w = scene.getWidth();
        double h = scene.getHeight();

        bgContext.save();

        if (shakeIntensity > 0) {
            double dx = (random.nextDouble() - 0.5) * 2 * shakeIntensity;
            double dy = (random.nextDouble() - 0.5) * 2 * shakeIntensity;
            bgContext.translate(dx, dy);
        }

        double gridPixelSize = NBcases * LENcases;

        if (comicMode) {
            bgContext.setFill(Color.web("#FDF6E2"));
            bgContext.fillRect(-40, -40, w + 80, h + 80);

            bgContext.setFill(Color.web("#EF4444", 0.14));
            for (double dx = 0; dx < w; dx += 24) {
                for (double dy = 0; dy < h; dy += 24) {
                    bgContext.fillOval(dx, dy, 6, 6);
                }
            }

            bgContext.setStroke(Color.web("#1E293B", 0.12));
            bgContext.setLineWidth(2.5);
            double cx = w / 2.0, cy = h / 2.0;
            for (int a = 0; a < 360; a += 12) {
                double rad = Math.toRadians(a);
                bgContext.strokeLine(cx + Math.cos(rad) * 360, cy + Math.sin(rad) * 360, cx + Math.cos(rad) * 950, cy + Math.sin(rad) * 950);
            }

            bgContext.setFill(Color.BLACK);
            bgContext.fillRect(ORIGIN[0] + 4, ORIGIN[1] + 4, gridPixelSize + 8, gridPixelSize + 8);
            bgContext.setFill(redTeamMode ? Color.web("#FF2A6D") : Color.web("#FACC15"));
            bgContext.fillRect(ORIGIN[0] - 6, ORIGIN[1] - 6, gridPixelSize + 12, gridPixelSize + 12);
            bgContext.setStroke(Color.BLACK);
            bgContext.setLineWidth(4.5);
            bgContext.strokeRect(ORIGIN[0] - 6, ORIGIN[1] - 6, gridPixelSize + 12, gridPixelSize + 12);
        } else {
            bgContext.setFill(Color.web("#050811"));
            bgContext.fillRect(-40, -40, w + 80, h + 80);

            bgContext.setStroke(Color.web("#0F172A", 0.85));
            bgContext.setLineWidth(1.0);
            double gridOffset = (time * 22) % 40;
            for (double gx = gridOffset; gx < w; gx += 40) bgContext.strokeLine(gx, 0, gx, h);
            for (double gy = gridOffset; gy < h; gy += 40) bgContext.strokeLine(0, gy, w, gy);

            Color frameColor = (rewindEffectTimer > 0) ? Color.web("#38BDF8", 1.0)
                    : (orbitalBeamActive) ? Color.web("#FF2A6D", 1.0)
                    : (hyperSurgeTimer > 0) ? Color.web("#FACC15", 0.95)
                    : (redTeamMode) ? Color.web("#FF2A6D", 0.75) : Color.web("#05D5FA", 0.65);
            bgContext.setStroke(frameColor);
            bgContext.setLineWidth(hyperSurgeTimer > 0 || orbitalBeamActive || rewindEffectTimer > 0 ? 4.5 : 3.0);
            bgContext.strokeRoundRect(ORIGIN[0] - 6, ORIGIN[1] - 6, gridPixelSize + 12, gridPixelSize + 12, 14, 14);
        }

        double pulse = 0.90 + 0.10 * Math.sin(time * 7.5);

        // 3. RENDU DES CELLULES
        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                Cell cell = grid[y][x];
                CellState state = cell.getState();

                double posX = LENcases * x + ORIGIN[0];
                double posY = LENcases * y + ORIGIN[1];

                double visibility = 1.0;
                if (blackoutTimer > 0) {
                    double cellCx = posX + LENcases / 2.0;
                    double cellCy = posY + LENcases / 2.0;
                    double distMouse = Math.hypot(cellCx - mouseX, cellCy - mouseY);
                    visibility = (distMouse <= LENcases * 5.2 || state == CellState.TURRET || state == CellState.DATA_CORE
                            || state == CellState.HONEYPOT || state == CellState.ANTIVIRUS) ? 1.0 : 0.18;
                }

                if (comicMode) {
                    bgContext.setFill(CellState.EMPTY.getComicColor().deriveColor(0, 1, 1, visibility));
                    bgContext.fillRect(posX, posY, LENcases, LENcases);
                    bgContext.setStroke(Color.BLACK);
                    bgContext.setLineWidth(1.8);
                    bgContext.strokeRect(posX, posY, LENcases, LENcases);
                } else {
                    bgContext.setFill(CellState.EMPTY.getBaseColor().deriveColor(0, 1, 1, visibility));
                    bgContext.fillRoundRect(posX + 1.5, posY + 1.5, LENcases - 3, LENcases - 3, 6, 6);
                    bgContext.setStroke(CellState.EMPTY.getHighlightColor().deriveColor(0, 1, 1, visibility));
                    bgContext.setLineWidth(1.0);
                    bgContext.strokeRoundRect(posX + 1.5, posY + 1.5, LENcases - 3, LENcases - 3, 6, 6);
                }

                if (state != CellState.EMPTY) {
                    double scale = cell.getScale();
                    if (state.isMalware() || state == CellState.DATA_CORE || state == CellState.PLASMA_MINE
                            || state == CellState.HONEYPOT || state == CellState.ANTIVIRUS) {
                        scale *= pulse;
                    }

                    double size = (LENcases - 3) * scale;
                    double offset = (LENcases - size) / 2.0;

                    if (comicMode) {
                        Color activeColor = state.getComicColor();
                        bgContext.setFill(Color.BLACK);
                        bgContext.fillRoundRect(posX + offset + 2, posY + offset + 2, size, size, 6, 6);
                        bgContext.setFill(activeColor.deriveColor(0, 1, 1, visibility));
                        bgContext.fillRoundRect(posX + offset, posY + offset, size, size, 6, 6);
                        bgContext.setStroke(Color.BLACK);
                        bgContext.setLineWidth(2.2);
                        bgContext.strokeRoundRect(posX + offset, posY + offset, size, size, 6, 6);
                    } else {
                        bgContext.setFill(state.getBaseColor().deriveColor(0, 1, 1, visibility));
                        bgContext.fillRoundRect(posX + offset, posY + offset, size, size, 7, 7);
                        bgContext.setStroke(state.getHighlightColor().deriveColor(0, 1, 1, visibility));
                        bgContext.setLineWidth(1.8);
                        bgContext.strokeRoundRect(posX + offset + 1, posY + offset + 1, size - 2, size - 2, 6, 6);
                    }

                    if (state == CellState.DATA_CORE) {
                        bgContext.setFill(Color.WHITE);
                        bgContext.fillOval(posX + LENcases / 2.0 - 4, posY + LENcases / 2.0 - 4, 8, 8);
                    } else if (state == CellState.PLASMA_MINE) {
                        bgContext.setStroke(Color.WHITE);
                        bgContext.strokeLine(posX + 6, posY + LENcases / 2.0, posX + LENcases - 6, posY + LENcases / 2.0);
                        bgContext.strokeLine(posX + LENcases / 2.0, posY + 6, posX + LENcases / 2.0, posY + LENcases - 6);
                    } else if (state == CellState.HONEYPOT) {
                        double magRadius = (LENcases * 0.45) + (time * 18) % (LENcases * 0.65);
                        bgContext.setStroke(Color.web("#FACC15", 0.75));
                        bgContext.setLineWidth(1.6);
                        bgContext.strokeOval(posX + LENcases / 2.0 - magRadius, posY + LENcases / 2.0 - magRadius, magRadius * 2, magRadius * 2);
                        bgContext.setFill(Color.BLACK);
                        bgContext.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
                        bgContext.fillText("" + (3 - cell.honeypotAbsorbed), posX + LENcases * 0.35, posY + LENcases * 0.70);
                    } else if (state == CellState.ANTIVIRUS) {
                        bgContext.setStroke(Color.WHITE);
                        bgContext.setLineWidth(2.0);
                        bgContext.strokeOval(posX + 6, posY + 6, LENcases - 12, LENcases - 12);
                    } else if (state == CellState.PORTAL_A || state == CellState.PORTAL_B) {
                        double r = LENcases * 0.35;
                        bgContext.setStroke(Color.WHITE);
                        bgContext.setLineWidth(2.2);
                        bgContext.strokeOval(posX + LENcases / 2.0 - r, posY + LENcases / 2.0 - r, r * 2, r * 2);
                    }
                }
            }
        }

        for (int[] link : activeTeslaLinks) {
            double x1 = ORIGIN[0] + link[0] * LENcases + LENcases / 2.0;
            double y1 = ORIGIN[1] + link[1] * LENcases + LENcases / 2.0;
            double x2 = ORIGIN[0] + link[2] * LENcases + LENcases / 2.0;
            double y2 = ORIGIN[1] + link[3] * LENcases + LENcases / 2.0;

            bgContext.setStroke(Color.web("#38BDF8", 0.8));
            bgContext.setLineWidth(4.0);
            bgContext.strokeLine(x1, y1, x2, y2);
            bgContext.setStroke(Color.WHITE);
            bgContext.setLineWidth(1.8);
            double midX = (x1 + x2) / 2.0 + (random.nextDouble() - 0.5) * 9;
            double midY = (y1 + y2) / 2.0 + (random.nextDouble() - 0.5) * 9;
            bgContext.strokeLine(x1, y1, midX, midY);
            bgContext.strokeLine(midX, midY, x2, y2);
        }

        if (orbitalBeamActive) {
            bgContext.setFill(Color.web("#FF2A6D", 0.4));
            bgContext.fillRect(ORIGIN[0] - 20, orbitalBeamY - 22, gridPixelSize + 40, 44);
            bgContext.setFill(Color.web("#FACC15", 0.9));
            bgContext.fillRect(ORIGIN[0] - 15, orbitalBeamY - 9, gridPixelSize + 30, 18);
            bgContext.setFill(Color.WHITE);
            bgContext.fillRect(ORIGIN[0] - 10, orbitalBeamY - 3, gridPixelSize + 20, 6);
        }

        if (rewindEffectTimer > 0) {
            bgContext.setFill(Color.web("#38BDF8", 0.16 * rewindEffectTimer));
            bgContext.fillRect(ORIGIN[0], ORIGIN[1], gridPixelSize, gridPixelSize);
            for (int i = 0; i < 8; i++) {
                double glitchY = ORIGIN[1] + random.nextDouble() * gridPixelSize;
                bgContext.setFill(i % 2 == 0 ? Color.web("#05D5FA", 0.55) : Color.web("#FF2A6D", 0.55));
                bgContext.fillRect(ORIGIN[0] - 10, glitchY, gridPixelSize + 20, 4 + random.nextDouble() * 8);
            }
        }

        if (empFreezeTimer > 0) {
            bgContext.setFill(Color.web("#38BDF8", 0.18));
            bgContext.fillRect(ORIGIN[0], ORIGIN[1], gridPixelSize, gridPixelSize);
        }

        // 4. Réticule de visée
        if (hoverX >= 0 && hoverX < NBcases && hoverY >= 0 && hoverY < NBcases && currentPlan == Plan.GAMEPLAY) {
            if (selectedTool == 2) {
                double rx = ORIGIN[0] + (hoverX - 1) * LENcases;
                double ry = ORIGIN[1] + (hoverY - 1) * LENcases;
                bgContext.setStroke(Color.web("#FACC15", 0.95));
                bgContext.setLineWidth(2.8);
                bgContext.strokeRoundRect(rx, ry, LENcases * 3, LENcases * 3, 8, 8);
            } else {
                double hx = LENcases * hoverX + ORIGIN[0];
                double hy = LENcases * hoverY + ORIGIN[1];
                bgContext.setStroke(redTeamMode ? Color.web("#FF2A6D") : (hyperSurgeTimer > 0 ? Color.web("#FACC15") : Color.web("#05D5FA")));
                bgContext.setLineWidth(2.5);
                bgContext.strokeRoundRect(hx + 1, hy + 1, LENcases - 2, LENcases - 2, 6, 6);
            }
        }

        // 5. Entités
        for (AllyDrone d : drones) d.render(bgContext, comicMode);
        for (SupplyDrop sd : supplyDrops) sd.render(bgContext, time, comicMode);
        for (VirusMissile vm : virusMissiles) {
            double tx = ORIGIN[0] + vm.targetGx * LENcases + LENcases / 2.0;
            double ty = ORIGIN[1] + vm.targetGy * LENcases + LENcases / 2.0;
            vm.render(bgContext, tx, ty, time, comicMode);
        }
        for (SwarmProjectile sp : swarmProjectiles) sp.render(bgContext);
        for (Shockwave s : shockwaves) s.render(bgContext);
        for (CyberLaser l : lasers) l.render(bgContext);
        for (Particle p : particles) p.render(bgContext);

        // 6. HUD adaptatif + Pop-ups latéraux qui ne cachent jamais la grille !
        drawAdaptiveHUD();
        for (FloatingText ft : floatingTexts) ft.render(bgContext);

        if (screenFlashAlpha > 0) {
            bgContext.setFill(screenFlashColor.deriveColor(0, 1, 1, screenFlashAlpha));
            bgContext.fillRect(0, 0, w, h);
        }

        bgContext.restore();
    }

    private void drawStyledBox(double x, double y, double w, double h, Color fill, Color borderColor) {
        if (comicMode) {
            bgContext.setFill(Color.BLACK);
            bgContext.fillRoundRect(x + 4, y + 4, w, h, 8, 8);
            bgContext.setFill(fill);
            bgContext.fillRoundRect(x, y, w, h, 8, 8);
            bgContext.setStroke(Color.BLACK);
            bgContext.setLineWidth(3.0);
            bgContext.strokeRoundRect(x, y, w, h, 8, 8);
        } else {
            bgContext.setFill(Color.web("#0F172A", 0.90));
            bgContext.fillRoundRect(x, y, w, h, 10, 10);
            bgContext.setStroke(borderColor);
            bgContext.setLineWidth(1.6);
            bgContext.strokeRoundRect(x, y, w, h, 10, 10);
        }
    }

    private void drawAdaptiveHUD() {
        double w = scene.getWidth();
        double gridRight = ORIGIN[0] + NBcases * LENcases;
        double topY = Math.max(22, ORIGIN[1] - 96);

        Color panelBg = comicMode ? Color.web("#FACC15") : Color.web("#0F172A");
        Color textPrimary = comicMode ? Color.BLACK : Color.web("#E2E8F0");

        // Haut-Gauche : Ressource
        double bwX = Math.max(18, ORIGIN[0] - 275);
        Color accentLeft = redTeamMode ? Color.web("#FF2A6D") : Color.web("#05D5FA");
        drawStyledBox(bwX - 8, topY - 18, 256, 68, panelBg, accentLeft);
        String resName = redTeamMode ? "PUISSANCE TFLOPS : " : "BANDWIDTH : ";
        drawHudText(resName + (int) bandwidth + " / " + (int) maxBandwidth, bwX, topY, comicMode ? Color.BLACK : accentLeft, 13);

        bgContext.setFill(Color.web("#1E293B"));
        bgContext.fillRoundRect(bwX, topY + 6, 236, 15, 6, 6);
        double bwRatio = Math.min(1.0, bandwidth / maxBandwidth);
        bgContext.setFill(hyperSurgeTimer > 0 ? Color.web("#FACC15") : accentLeft);
        bgContext.fillRoundRect(bwX + 2, topY + 8, 232 * bwRatio, 11, 5, 5);

        drawHudText(
                redTeamMode
                        ? "☣️ CELLULES:" + countTotalMalwareCells() + " | ⏪ POLYMORPHE:" + rollbackCharges
                        : "🛡️ BOUCLIERS:" + perimeterShields + " | ⏪ ROLLBACK:" + rollbackCharges,
                bwX,
                topY + 38,
                textPrimary,
                11
        );

        // Haut-Centre : Statut de la Mission
        double centerBoxW = 430;
        double centerBoxX = (w - centerBoxW) / 2.0;
        drawStyledBox(centerBoxX, topY - 18, centerBoxW, 68, comicMode ? Color.web("#E62429") : Color.web("#0F172A"), Color.web("#FF2A6D"));

        if (medalBannerTimer > 0) {
            drawHudText(activeMedalBanner, centerBoxX + 12, topY + 18, Color.web("#FACC15"), 13);
        } else if (redTeamMode) {
            int remainingCores = countCells(CellState.DATA_CORE);
            drawHudText("🔴 MODE RED TEAM (TU ES LE VIRUS) — NIVEAU " + wave, centerBoxX + 16, topY + 4, Color.web("#FF2A6D"), 14);
            drawHudText("OBJECTIF : Atteins le bord extérieur OU pirate les Serveurs (" + (3 - remainingCores) + "/3) !", centerBoxX + 16, topY + 28, Color.WHITE, 11);
        } else {
            String modeTitle = comicMode
                    ? "💥 NIVEAU " + wave + " — MODE BANDE DESSINÉE ACTIF ! 💥"
                    : "⚡ NIVEAU " + wave + " — MODE FUTURISTE CYBERPUNK ACTIF ! ⚡";
            drawHudText(modeTitle, centerBoxX + 18, topY + 6, Color.web("#FACC15"), 14);
            drawHudText("Virus actifs : " + countTotalMalwareCells() + " | Encercle une poche ou clique pour purger !", centerBoxX + 18, topY + 30, Color.WHITE, 11);
        }

        // Haut-Droite : Score, Crédits & Rang
        double scoreX = gridRight + 22;
        drawStyledBox(scoreX - 8, topY - 18, 256, 68, panelBg, Color.web("#4ADE80"));
        drawHudText("SCORE : " + score + " XP | " + credits + "$", scoreX, topY, comicMode ? Color.BLACK : Color.web("#4ADE80"), 14);
        drawHudText("🎖️ " + RANK_NAMES[currentRankIndex], scoreX, topY + 20, comicMode ? Color.web("#B91C1C") : Color.web("#FACC15"), 13);
        drawHudText("🏅 MÉDAILLES : " + totalMedalsEarned + " | NIVEAU " + wave, scoreX, topY + 38, textPrimary, 12);

        drawLeftInfoPanel(bwX - 8, ORIGIN[1]);
        drawRightTacticalPanel(scoreX - 8, ORIGIN[1]);
        drawTacticalHotbar();
    }

    private void drawLeftInfoPanel(double px, double py) {
        Color boxBg = comicMode ? Color.WHITE : Color.web("#0F172A");
        Color txtColor = comicMode ? Color.BLACK : Color.WHITE;
        drawStyledBox(px, py, 256, 390, boxBg, redTeamMode ? Color.web("#FF2A6D") : Color.web("#05D5FA"));

        drawHudText(redTeamMode ? "// GUIDE HACKER RED TEAM //" : "// GUIDE DÉFENSEUR SOC //", px + 12, py + 24, comicMode ? Color.web("#E62429") : Color.web("#05D5FA"), 13);

        String[][] guide = redTeamMode
                ? new String[][]{
                {"[1] INJECTION", "Étend ton virus / Brise Mur", "#FF2A6D"},
                {"[2] BOMBE 3x3", "Détruit Murs IA en zone", "#FACC15"},
                {"[3] ATTAQUE DDoS", "Désactive l'IA pendant 6s", "#38BDF8"},
                {"[4] SAUT TROJAN", "Saute par-dessus les murs", "#A855F7"},
                {"[5] VER WORM", "Fonce vers la bordure", "#84CC16"},
                {"[6] 3 MISSILES", "Bombarde les défenses IA", "#F97316"}
        }
                : new String[][]{
                {"ENCERCLEMENT", "Ferme une boucle = Implosion", "#05D5FA"},
                {"[6] POT DE MIEL", "Attire Virus -> Antivirus", "#F59E0B"},
                {"ANTIVIRUS VERT", "Dévore les virus ennemis", "#00FF9D"},
                {"[R] ROLLBACK", "Remonte le temps de 4.0s", "#38BDF8"},
                {"NIVEAUX PAIRS", "Bascule en Style BD !", "#E62429"},
                {"FOYERS MULTIPLES", "Plus de virus à chaque vague", "#FACC15"}
        };

        for (int i = 0; i < guide.length; i++) {
            double y = py + 44 + i * 52;
            bgContext.setFill(Color.web(guide[i][2]));
            bgContext.fillRoundRect(px + 12, y, 16, 16, 4, 4);

            drawHudText(guide[i][0], px + 36, y + 13, txtColor, 13);
            bgContext.setFill(comicMode ? Color.web("#334155") : Color.web("#94A3B8"));
            bgContext.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
            bgContext.fillText(guide[i][1], px + 36, y + 29);
        }
    }

    private void drawRightTacticalPanel(double rx, double ry) {
        Color boxBg = comicMode ? Color.WHITE : Color.web("#0F172A");
        Color txtColor = comicMode ? Color.BLACK : Color.WHITE;
        drawStyledBox(rx, ry, 256, 265, boxBg, Color.web("#FACC15"));

        double speedMult = Math.round((getCurrentSpreadRate() / BASE_SPREAD_RATE) * 100.0) / 100.0;
        drawHudText("⚡ VITESSE VIRUS : x" + speedMult, rx + 14, ry + 24, comicMode ? Color.web("#E62429") : Color.web("#FF2A6D"), 14);

        double sy = ry + 50;
        drawHudText("SURCHARGE GRATUITE (" + (int) hyperSurgeDuration + "s)", rx + 14, sy, txtColor, 12);
        bgContext.setFill(Color.web("#1E293B"));
        bgContext.fillRoundRect(rx + 14, sy + 8, 228, 16, 6, 6);

        if (hyperSurgeTimer > 0) {
            double ratio = hyperSurgeTimer / hyperSurgeDuration;
            bgContext.setFill(Color.web("#FACC15"));
            bgContext.fillRoundRect(rx + 16, sy + 10, 224 * ratio, 12, 5, 5);
            drawHudText("🔥 GLISSE LA SOURIS : " + String.format("%.1f", hyperSurgeTimer) + "s", rx + 14, sy + 40, Color.web("#D97706"), 12);
        } else {
            double ratio = hyperSurgeCharge / HYPER_SURGE_COOLDOWN_MAX;
            bgContext.setFill(Color.web("#05D5FA"));
            bgContext.fillRoundRect(rx + 16, sy + 10, 224 * ratio, 12, 5, 5);
            drawHudText("Prêt dans " + String.format("%.1f", HYPER_SURGE_COOLDOWN_MAX - hyperSurgeCharge) + "s", rx + 14, sy + 40, txtColor, 11);
        }

        double my = ry + 125;
        drawHudText(redTeamMode ? "☣️ ÉTAT DE L'INFILTRATION :" : "🔓 ARSENAL PAR NIVEAU :", rx + 14, my, comicMode ? Color.web("#1D4ED8") : Color.web("#4ADE80"), 12);
        bgContext.setFill(txtColor);
        bgContext.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        if (redTeamMode) {
            bgContext.fillText("• Serveurs restants : " + countCells(CellState.DATA_CORE) + "/3", rx + 14, my + 20);
            bgContext.fillText("• Murs IA actifs    : " + countCells(CellState.FIREWALL), rx + 14, my + 38);
            bgContext.fillText("• Tourelles IA      : " + countCells(CellState.TURRET), rx + 14, my + 56);
            bgContext.fillText("• Statut IA SOC     : " + (empFreezeTimer > 0 ? "DDoS GELÉE ❄️" : "ACTIVE ⚠️"), rx + 14, my + 74);
        } else {
            bgContext.fillText("• Pot de Miel & Rollback : ACTIF ✅", rx + 14, my + 20);
            bgContext.fillText("• Mines & Missiles : " + (wave >= 2 ? "ACTIF ✅" : "Niveau 2 🔒"), rx + 14, my + 38);
            bgContext.fillText("• Tourelles Tesla  : " + (wave >= 3 ? "ACTIF ✅" : "Niveau 3 🔒"), rx + 14, my + 56);
            bgContext.fillText("• Portails & Drones: " + (wave >= 4 ? "ACTIF ✅" : "Niveau 4 🔒"), rx + 14, my + 74);
        }

        // BOUTON ROUGE
        double redY = ry + 280;
        boolean unlocked = redTeamMode || (wave >= 2);
        boolean ready = unlocked && orbitalCharge >= 100.0;
        drawStyledBox(rx, redY, 256, 56, ready ? Color.web("#FACC15") : Color.web("#991B1B"), Color.web("#FF2A6D"));
        drawHudText(redTeamMode ? "☣️ ONDE ZERO-DAY [ESPACE]" : "☢️ BOUTON ROUGE [ESPACE]", rx + 14, redY + 22, (ready && comicMode) ? Color.BLACK : Color.WHITE, 13);
        String btnSub = !unlocked ? "🔒 DÉBLOQUÉ AU NIVEAU 2" : (ready ? ">> PRÊT ! CLIQUE OU ESPACE <<" : "CHARGE : " + (int) orbitalCharge + "%");
        drawHudText(btnSub, rx + 14, redY + 42, (ready && comicMode) ? Color.BLACK : Color.web("#FEF08A"), 11);

        // BOUTON ROLLBACK / MUTATION POLYMORPHE [R]
        double rbY = ry + 346;
        boolean rbReady = rollbackCharges > 0 && !snapshotHistory.isEmpty();
        drawStyledBox(rx, rbY, 256, 52, rbReady ? Color.web("#0284C7") : Color.web("#1E293B"), Color.web("#38BDF8"));
        drawHudText(redTeamMode ? "⏪ POLYMORPHE [R]" : "⏪ ROLLBACK TEMPOREL [R]", rx + 14, rbY + 21, Color.WHITE, 13);
        drawHudText("Remonte de 4s (Charges : " + rollbackCharges + ")", rx + 14, rbY + 40, Color.web("#BAE6FD"), 11);
    }

    private void drawTacticalHotbar() {
        String[][] tools = redTeamMode
                ? new String[][]{
                {"[1] INJECTION VIRALE", "10 TF Infecte | 12 TF Sabote", "#FF2A6D", "1"},
                {"[2] BOMBE LOGIQUE 3x3", (int) nukeCost + " TF | Brise Murs IA", "#FACC15", "1"},
                {"[3] ATTAQUE DDoS", "45 TF | Bloque IA 6.0s", "#38BDF8", "1"},
                {"[4] SAUT TROJAN 🐴", "25 TF | Saute les Murs", "#A855F7", "1"},
                {"[5] VER WORM 🐛", "25 TF | Fonce au bord", "#84CC16", "1"},
                {"[6] 3 MISSILES 🚀", "30 TF | Salve Virale", "#F97316", "1"}
        }
                : new String[][]{
                {"[1] FIREWALL / LASER", "12 BW Mur | 18 BW Kill", "#05D5FA", "1"},
                {"[2] NUKE 3x3 + ESSAIM", (int) nukeCost + " BW | Explosion Zone", "#FACC15", "1"},
                {"[3] EMP CRYO-FREEZE", wave >= 3 ? "45 BW | Gel 6.0s" : "🔒 Niveau 3", "#38BDF8", "3"},
                {"[4] TOURELLE TESLA", wave >= 3 ? "40 BW | Arc Tesla" : "🔒 Niveau 3", "#10B981", "3"},
                {"[5] MINE PLASMA (+)", wave >= 2 ? "28 BW | Croix (+)" : "🔒 Niveau 2", "#D946EF", "2"},
                {"[6] POT DE MIEL 🍯", "28 BW | Aimant➔Antivirus", "#F59E0B", "1"}
        };

        double cardW = 166;
        double cardH = 58;
        double gap = 10;
        double totalW = 6 * cardW + 5 * gap;
        double startX = (scene.getWidth() - totalW) / 2.0;
        double y = ORIGIN[1] + NBcases * LENcases + 16;

        for (int i = 0; i < 6; i++) {
            double x = startX + i * (cardW + gap);
            int reqWave = Integer.parseInt(tools[i][3]);
            boolean unlocked = (wave >= reqWave);
            boolean active = unlocked && ((selectedTool == i + 1) || (i == 2 && empFreezeTimer > 0));
            Color theme = Color.web(tools[i][2]);

            Color cardFill = !unlocked ? Color.web("#334155") : (active ? theme : (comicMode ? Color.WHITE : Color.web("#0F172A")));
            drawStyledBox(x, y, cardW, cardH, cardFill, active ? theme : Color.web("#334155"));

            Color titleColor = !unlocked ? Color.web("#94A3B8") : (comicMode ? Color.BLACK : theme);
            drawHudText(tools[i][0], x + 8, y + 22, titleColor, 12);

            bgContext.setFill(!unlocked ? Color.web("#CBD5E1") : (comicMode ? Color.web("#1E293B") : Color.web("#CBD5E1")));
            bgContext.setFont(Font.font("Consolas", FontWeight.BOLD, 10));
            bgContext.fillText(tools[i][1], x + 8, y + 41);
        }
    }

    private void drawHudText(String text, double x, double y, Color color, int size) {
        bgContext.setFont(Font.font("Consolas", FontWeight.BOLD, size));
        bgContext.setFill(color);
        bgContext.fillText(text, x, y);
    }

    private int countCells(CellState target) {
        int count = 0;
        for (int y = 0; y < NBcases; y++) {
            for (int x = 0; x < NBcases; x++) {
                if (grid[y][x].getState() == target) count++;
            }
        }
        return count;
    }

    // =========================================================================
    // MOTEUR AUDIO SYNTHÉTISEUR CYBERPUNK 126 BPM & BRUITAGES
    // =========================================================================

    public static class CyberAudioEngine {
        private volatile boolean running = false;
        private volatile boolean musicActive = false;
        private volatile boolean muted = false;
        private final ConcurrentLinkedQueue<SfxVoice> sfxQueue = new ConcurrentLinkedQueue<>();
        private Thread audioThread;

        private static class SfxVoice {
            int type;
            double elapsed = 0;
            double duration;

            SfxVoice(int type, double duration) {
                this.type = type;
                this.duration = duration;
            }
        }

        public void startEngine() {
            if (running) return;
            running = true;
            audioThread = new Thread(this::runSynthLoop, "CyberSynth-Thread");
            audioThread.setDaemon(true);
            audioThread.start();
        }

        public void stopEngine() {
            running = false;
        }

        public void setMusicActive(boolean active) {
            this.musicActive = active;
        }

        public void toggleMute() {
            this.muted = !this.muted;
        }

        public boolean isMuted() {
            return muted;
        }

        public void playLaser() {
            if (!muted) sfxQueue.offer(new SfxVoice(1, 0.14));
        }

        public void playExplosion() {
            if (!muted) sfxQueue.offer(new SfxVoice(2, 0.28));
        }

        public void playPowerup() {
            if (!muted) sfxQueue.offer(new SfxVoice(3, 0.45));
        }

        public void playNuke() {
            if (!muted) sfxQueue.offer(new SfxVoice(4, 0.65));
        }

        public void playOrbitalBeam() {
            if (!muted) sfxQueue.offer(new SfxVoice(5, 1.80));
        }

        public void playGameOver() {
            if (!muted) sfxQueue.offer(new SfxVoice(6, 0.90));
        }

        public void playRollback() {
            if (!muted) sfxQueue.offer(new SfxVoice(7, 0.75));
        }

        private void runSynthLoop() {
            try {
                Class<?> audioFormatClass = Class.forName("javax.sound.sampled.AudioFormat");
                HelloApplication.class.getModule().addReads(audioFormatClass.getModule());
                Class<?> audioSystemClass = Class.forName("javax.sound.sampled.AudioSystem");
                Class<?> sourceDataLineClass = Class.forName("javax.sound.sampled.SourceDataLine");

                float sampleRate = 22050f;
                Constructor<?> fmtCtor = audioFormatClass.getConstructor(float.class, int.class, int.class, boolean.class, boolean.class);
                Object format = fmtCtor.newInstance(sampleRate, 16, 1, true, false);

                Method getSourceDataLine = audioSystemClass.getMethod("getSourceDataLine", audioFormatClass);
                Object line = getSourceDataLine.invoke(null, format);

                Method openMethod = sourceDataLineClass.getMethod("open", audioFormatClass, int.class);
                Method startMethod = sourceDataLineClass.getMethod("start");
                Method writeMethod = sourceDataLineClass.getMethod("write", byte[].class, int.class, int.class);
                Method closeMethod = sourceDataLineClass.getMethod("close");

                openMethod.invoke(line, format, 4096);
                startMethod.invoke(line);

                int chunkSamples = 512;
                byte[] pcmBuffer = new byte[chunkSamples * 2];
                List<SfxVoice> activeVoices = new ArrayList<>();

                double[] bassFreqs = {73.42, 73.42, 87.31, 73.42, 110.00, 98.00, 87.31, 65.41};
                double[] arpFreqs = {293.66, 349.23, 440.00, 587.33, 440.00, 349.23, 523.25, 440.00};
                double globalTime = 0;
                double dt = 1.0 / sampleRate;

                while (running) {
                    SfxVoice v;
                    while ((v = sfxQueue.poll()) != null) {
                        if (activeVoices.size() < 12) activeVoices.add(v);
                    }

                    for (int i = 0; i < chunkSamples; i++) {
                        double sample = 0.0;

                        if (!muted && musicActive) {
                            double stepDuration = 60.0 / 126.0 / 4.0;
                            int stepIndex = (int) (globalTime / stepDuration);
                            double stepPos = (globalTime % stepDuration) / stepDuration;

                            if (stepIndex % 4 == 0) {
                                double kickEnv = Math.exp(-stepPos * 9.5);
                                double kickFreq = 42.0 + 110.0 * Math.exp(-stepPos * 22.0);
                                sample += Math.sin(2 * Math.PI * kickFreq * (stepPos * stepDuration)) * kickEnv * 0.42;
                            }

                            if (stepIndex % 4 == 2) {
                                double hatEnv = Math.exp(-stepPos * 18.0);
                                sample += (Math.random() * 2.0 - 1.0) * hatEnv * 0.12;
                            }

                            double bassFreq = bassFreqs[(stepIndex / 2) % bassFreqs.length];
                            double bassEnv = Math.exp(-stepPos * 4.5);
                            double saw = 2.0 * ((globalTime * bassFreq) - Math.floor(0.5 + globalTime * bassFreq));
                            sample += saw * bassEnv * 0.22;

                            double arpFreq = arpFreqs[stepIndex % arpFreqs.length];
                            double arpEnv = Math.exp(-stepPos * 7.0);
                            double sq = Math.sin(2 * Math.PI * arpFreq * globalTime) >= 0 ? 0.8 : -0.8;
                            sample += sq * arpEnv * 0.10;
                        }

                        if (!muted && !activeVoices.isEmpty()) {
                            Iterator<SfxVoice> it = activeVoices.iterator();
                            while (it.hasNext()) {
                                SfxVoice sv = it.next();
                                sv.elapsed += dt;
                                if (sv.elapsed >= sv.duration) {
                                    it.remove();
                                    continue;
                                }
                                double p = sv.elapsed / sv.duration;
                                double env = 1.0 - p;

                                switch (sv.type) {
                                    case 1 -> {
                                        double f = 980.0 - p * 760.0;
                                        sample += Math.sin(2 * Math.PI * f * sv.elapsed) * env * 0.32;
                                    }
                                    case 2 -> {
                                        double noise = (Math.random() * 2.0 - 1.0);
                                        double boom = Math.sin(2 * Math.PI * (85.0 - p * 55.0) * sv.elapsed);
                                        sample += (noise * 0.5 + boom * 0.5) * env * 0.38;
                                    }
                                    case 3 -> {
                                        double f = (p < 0.33) ? 523.25 : (p < 0.66 ? 659.25 : 783.99);
                                        sample += Math.sin(2 * Math.PI * f * sv.elapsed) * env * 0.30;
                                    }
                                    case 4 -> {
                                        double noise = (Math.random() * 2.0 - 1.0);
                                        double sub = Math.sin(2 * Math.PI * (65.0 - p * 40.0) * sv.elapsed);
                                        sample += (noise * 0.45 + sub * 0.65) * env * 0.48;
                                    }
                                    case 5 -> {
                                        double hum = Math.sin(2 * Math.PI * (110.0 + Math.sin(sv.elapsed * 25.0) * 35.0) * sv.elapsed);
                                        double sizzle = (Math.random() * 2.0 - 1.0) * 0.3;
                                        sample += (hum + sizzle) * env * 0.45;
                                    }
                                    case 6 -> {
                                        double f = 300.0 - p * 210.0;
                                        sample += Math.sin(2 * Math.PI * f * sv.elapsed) * env * 0.40;
                                    }
                                    case 7 -> {
                                        double f = 220.0 + p * 680.0 + Math.sin(sv.elapsed * 45.0) * 60.0;
                                        sample += Math.sin(2 * Math.PI * f * sv.elapsed) * env * 0.38;
                                    }
                                }
                            }
                        }

                        sample = Math.max(-0.95, Math.min(0.95, sample));
                        short val = (short) (sample * 32767);
                        pcmBuffer[i * 2] = (byte) (val & 0xFF);
                        pcmBuffer[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
                        globalTime += dt;
                    }

                    writeMethod.invoke(line, pcmBuffer, 0, pcmBuffer.length);
                }

                closeMethod.invoke(line);
            } catch (Exception ignored) {
            }
        }
    }

    // =========================================================================
    // CLASSES INTERNES (GridSnapshot, Drones, Airdrops, Missiles, Pop-ups)
    // =========================================================================

    public static class GridSnapshot {
        CellState[][] states = new CellState[NBcases][NBcases];

        public GridSnapshot(Cell[][] currentGrid) {
            for (int y = 0; y < NBcases; y++) {
                for (int x = 0; x < NBcases; x++) {
                    this.states[y][x] = currentGrid[y][x].getState();
                }
            }
        }
    }

    public static class AllyDrone {
        double x, y;
        double angle;
        boolean isHunter;
        double actionCooldown = 0.5;

        public AllyDrone(double startX, double startY, double startAngle, boolean isHunter) {
            this.x = startX;
            this.y = startY;
            this.angle = startAngle;
            this.isHunter = isHunter;
        }

        public void update(double dt, double centerX, double centerY, double radius) {
            angle += dt * (isHunter ? 1.85 : -1.45);
            x = centerX + Math.cos(angle) * radius;
            y = centerY + Math.sin(angle * 1.5) * (radius * 0.75);
            if (actionCooldown > 0) actionCooldown -= dt;
        }

        public void render(GraphicsContext gc, boolean comicMode) {
            Color c = isHunter ? Color.web("#FACC15") : Color.web("#05D5FA");
            if (comicMode) {
                gc.setFill(Color.BLACK);
                gc.fillOval(x - 11, y - 11, 22, 22);
            }
            gc.setStroke(c);
            gc.setLineWidth(2.2);
            gc.strokeOval(x - 9, y - 9, 18, 18);
            gc.setFill(c);
            gc.fillOval(x - 5, y - 5, 10, 10);
        }
    }

    public static class SupplyDrop {
        double x, y;

        public SupplyDrop(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void update(double dt) {
            y += dt * 52.0;
        }

        public void render(GraphicsContext gc, double time, boolean comicMode) {
            double swing = Math.sin(time * 5.0) * 4.0;
            gc.setStroke(comicMode ? Color.BLACK : Color.web("#38BDF8"));
            gc.setLineWidth(2.5);
            gc.strokeArc(x - 16 + swing, y - 24, 32, 20, 0, 180, javafx.scene.shape.ArcType.OPEN);

            gc.setFill(Color.web("#FACC15"));
            gc.fillRoundRect(x - 12, y - 6, 24, 24, 5, 5);
            gc.setStroke(comicMode ? Color.BLACK : Color.WHITE);
            gc.setLineWidth(2.0);
            gc.strokeRoundRect(x - 12, y - 6, 24, 24, 5, 5);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
            gc.fillText("$", x - 4, y + 12);
        }
    }

    public static class VirusMissile {
        double x, y;
        int targetGx, targetGy;
        double speed;
        boolean reachedTarget = false;

        public VirusMissile(double startX, double startY, int targetGx, int targetGy, double speed) {
            this.x = startX;
            this.y = startY;
            this.targetGx = targetGx;
            this.targetGy = targetGy;
            this.speed = speed;
        }

        public void update(double dt, double targetX, double targetY) {
            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.hypot(dx, dy);
            if (dist <= speed * dt) {
                x = targetX;
                y = targetY;
                reachedTarget = true;
            } else {
                x += (dx / dist) * speed * dt;
                y += (dy / dist) * speed * dt;
            }
        }

        public void render(GraphicsContext gc, double targetX, double targetY, double time, boolean comicMode) {
            gc.setStroke(comicMode ? Color.BLACK : Color.web("#FF2A6D", 0.4));
            gc.setLineWidth(1.8);
            gc.strokeLine(x, y, targetX, targetY);

            double pulse = 8 + 4 * Math.sin(time * 14);
            gc.setStroke(Color.web("#FACC15"));
            gc.setLineWidth(2.2);
            gc.strokeOval(targetX - pulse, targetY - pulse, pulse * 2, pulse * 2);

            if (comicMode) {
                gc.setFill(Color.BLACK);
                gc.fillOval(x - 10, y - 10, 20, 20);
            }
            gc.setFill(Color.web("#FF2A6D"));
            gc.fillOval(x - 7, y - 7, 14, 14);
            gc.setFill(Color.web("#FACC15"));
            gc.fillOval(x - 3.5, y - 3.5, 7, 7);
        }
    }

    public static class SwarmProjectile {
        double startX, startY, targetX, targetY;
        double x, y;
        double curveOffset;
        double progress = 0.0;
        Color color;

        public SwarmProjectile(double startX, double startY, double targetX, double targetY, double curveOffset, Color color) {
            this.startX = startX;
            this.startY = startY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.curveOffset = curveOffset;
            this.color = color;
            this.x = startX;
            this.y = startY;
        }

        public void update(double dt) {
            progress = Math.min(1.0, progress + dt * 2.6);
            double linearX = startX + (targetX - startX) * progress;
            double linearY = startY + (targetY - startY) * progress;
            double arc = Math.sin(progress * Math.PI) * curveOffset;
            x = linearX + arc * 0.5;
            y = linearY - Math.abs(arc * 0.35);
        }

        public void render(GraphicsContext gc) {
            gc.setFill(color);
            gc.fillOval(x - 4.5, y - 4.5, 9, 9);
            gc.setFill(Color.WHITE);
            gc.fillOval(x - 2, y - 2, 4, 4);
        }
    }

    public static class Cell {
        private CellState state = CellState.EMPTY;
        private double scale = 1.0;
        public double turretCooldown = 0.5;
        public int honeypotAbsorbed = 0;
        public int antivirusLife = 4;

        public CellState getState() {
            return state;
        }

        public void setState(CellState newState) {
            if (this.state != newState) {
                this.state = newState;
                this.scale = 0.15;
            }
        }

        public void updateAnimation(double dt) {
            if (scale < 1.0) {
                scale = Math.min(1.0, scale + dt * 6.5);
            }
        }

        public double getScale() {
            return scale;
        }
    }

    public static class Particle {
        double x, y, vx, vy, size;
        double life = 1.0;
        Color color;
        boolean isSquare;

        public Particle(double x, double y, double vx, double vy, double size, Color color, boolean isSquare) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.color = color;
            this.isSquare = isSquare;
        }

        public void update(double dt) {
            x += vx * dt;
            y += vy * dt;
            vx *= 0.91;
            vy *= 0.91;
            life -= dt * 1.45;
        }

        public void render(GraphicsContext gc) {
            double alpha = Math.max(0, life);
            double s = size * alpha;
            gc.setFill(color.deriveColor(0, 1, 1, alpha));
            if (isSquare) {
                gc.fillRect(x - s / 2, y - s / 2, s, s);
            } else {
                gc.fillOval(x - s / 2, y - s / 2, s, s);
            }
        }
    }

    public static class Shockwave {
        double x, y, maxRadius;
        double life = 1.0;
        Color color;

        public Shockwave(double x, double y, double maxRadius, Color color) {
            this.x = x;
            this.y = y;
            this.maxRadius = maxRadius;
            this.color = color;
        }

        public void update(double dt) {
            life -= dt * 2.1;
        }

        public void render(GraphicsContext gc) {
            double progress = 1.0 - Math.max(0, life);
            double radius = progress * maxRadius;
            gc.setStroke(color.deriveColor(0, 1, 1, Math.max(0, life)));
            gc.setLineWidth(3.5 * Math.max(0, life));
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    public static class CyberLaser {
        double x1, y1, x2, y2, width;
        double life = 1.0;
        Color color;

        public CyberLaser(double x1, double y1, double x2, double y2, Color color, double width) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
            this.width = width;
        }

        public void update(double dt) {
            life -= dt * 4.5;
        }

        public void render(GraphicsContext gc) {
            double alpha = Math.max(0, life);
            gc.setStroke(color.deriveColor(0, 1, 1, alpha * 0.75));
            gc.setLineWidth(width * 2.2 * alpha);
            gc.strokeLine(x1, y1, x2, y2);

            gc.setStroke(Color.color(1, 1, 1, alpha));
            gc.setLineWidth(width * 0.7 * alpha);
            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    public static class FloatingText {
        double x, y, vx, vy;
        String text;
        Color color;
        int baseFontSize;
        PopupStyle style;
        double life = 1.0;
        double age = 0.0;

        public FloatingText(double x, double y, String text, Color color, int baseFontSize, PopupStyle style) {
            this.x = x;
            this.y = y;
            this.vx = (Math.random() - 0.5) * 18.0;
            this.vy = -52.0;
            this.text = text;
            this.color = color;
            this.baseFontSize = baseFontSize;
            this.style = style;
        }

        public void update(double dt) {
            age += dt;
            x += vx * dt;
            y += vy * dt;
            vy *= 0.95;
            life -= dt * 0.90;
        }

        public void render(GraphicsContext gc) {
            double alpha = Math.max(0, Math.min(1.0, life));
            double scale = age < 0.10 ? (0.5 + (age / 0.10) * 0.75) : (age < 0.20 ? 1.25 - ((age - 0.10) / 0.10) * 0.25 : 1.0);
            double fontSize = baseFontSize * scale;

            String fontFamily = (style == PopupStyle.STARBURST || style == PopupStyle.SPEECH_BUBBLE) ? "Impact" : "Consolas";
            gc.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize));

            double textW = text.length() * fontSize * (fontFamily.equals("Impact") ? 0.48 : 0.56);
            double padX = 12;
            double boxW = textW + padX * 2;
            double boxH = fontSize + 10;
            double cx = x;
            double cy = y - fontSize * 0.35;

            switch (style) {
                case STARBURST -> {
                    int points = 24;
                    double[] px = new double[points];
                    double[] py = new double[points];
                    double rxOuter = boxW * 0.62;
                    double ryOuter = boxH * 0.88;
                    double rxInner = boxW * 0.48;
                    double ryInner = boxH * 0.58;

                    for (int i = 0; i < points; i++) {
                        double ang = (i * Math.PI * 2.0) / points;
                        double rx = (i % 2 == 0) ? rxOuter : rxInner;
                        double ry = (i % 2 == 0) ? ryOuter : ryInner;
                        px[i] = cx + Math.cos(ang) * rx;
                        py[i] = cy + Math.sin(ang) * ry;
                    }

                    gc.setFill(color.deriveColor(0, 1, 1, alpha * 0.85));
                    gc.fillPolygon(px, py, points);
                    gc.setStroke(Color.color(0, 0, 0, alpha * 0.9));
                    gc.setLineWidth(2.2);
                    gc.strokePolygon(px, py, points);

                    gc.setFill(Color.color(0, 0, 0, alpha));
                    gc.fillText(text, cx - textW / 2.0, y);
                }

                case HEX_TECH -> {
                    double left = cx - boxW / 2.0;
                    double right = cx + boxW / 2.0;
                    double top = cy - boxH / 2.0;
                    double bottom = cy + boxH / 2.0;
                    double cut = 8.0;

                    double[] hx = {left + cut, right - cut, right, right - cut, left + cut, left};
                    double[] hy = {top, top, cy, bottom, bottom, cy};

                    gc.setFill(Color.color(0.04, 0.07, 0.15, alpha * 0.82));
                    gc.fillPolygon(hx, hy, 6);
                    gc.setStroke(color.deriveColor(0, 1, 1, alpha));
                    gc.setLineWidth(2.0);
                    gc.strokePolygon(hx, hy, 6);

                    gc.setFill(color.deriveColor(0, 1, 1, alpha));
                    gc.fillText(text, cx - textW / 2.0, y);
                }

                case SPEECH_BUBBLE -> {
                    double bx = cx - boxW / 2.0;
                    double by = cy - boxH / 2.0;

                    gc.setFill(Color.color(1, 1, 1, alpha * 0.88));
                    gc.fillRoundRect(bx, by, boxW, boxH, 14, 14);

                    gc.setStroke(color.deriveColor(0, 1, 0.85, alpha));
                    gc.setLineWidth(2.2);
                    gc.strokeRoundRect(bx, by, boxW, boxH, 14, 14);

                    gc.setFill(Color.color(0.05, 0.08, 0.15, alpha));
                    gc.fillText(text, cx - textW / 2.0, y);
                }

                case WARNING_SHIELD -> {
                    double left = cx - boxW / 2.0;
                    double right = cx + boxW / 2.0;
                    double top = cy - boxH / 2.0;
                    double bottom = cy + boxH / 2.0;
                    double c = 6.0;

                    double[] ox = {left + c, right - c, right, right, right - c, left + c, left, left};
                    double[] oy = {top, top, top + c, bottom - c, bottom, bottom, bottom - c, top + c};

                    gc.setFill(Color.color(0.12, 0.04, 0.08, alpha * 0.84));
                    gc.fillPolygon(ox, oy, 8);
                    gc.setStroke(color.deriveColor(0, 1, 1, alpha));
                    gc.setLineWidth(2.0);
                    gc.strokePolygon(ox, oy, 8);

                    gc.setFill(color.deriveColor(0, 1, 1, alpha));
                    gc.fillText(text, cx - textW / 2.0, y);
                }

                case NEON_FLOAT -> {
                    gc.setFill(Color.color(0, 0, 0, alpha * 0.85));
                    gc.fillText(text, cx - textW / 2.0 + 2, y + 2);
                    gc.setFill(color.deriveColor(0, 1, 1, alpha));
                    gc.fillText(text, cx - textW / 2.0, y);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}