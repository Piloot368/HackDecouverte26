package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class NanoSondeGame extends Application {

    public static final double SCREEN_WIDTH = 1150, SCREEN_HEIGHT = 700;

    // Zone de l'Artère / Fibre Optique (calculée dynamiquement au centre de l'écran)
    public double arenaX = 310, arenaY = 125, arenaW = 530, arenaH = 470;

    public enum ScreenState {
        ACCUEIL,
        GAMEPLAY,
        SHOP,
        GAME_OVER
    }

    public enum PopupStyle {
        NEON_FLOAT,
        STARBURST,
        HEX_TECH,
        SPEECH_BUBBLE,
        WARNING_SHIELD
    }

    public enum StrainType {
        ALPHA_CARMINE("#FF2A6D", "#F43F5E", 1.0, 1.0),
        SYNAPSE_VOLT("#A855F7", "#FACC15", 1.32, 0.85),
        ARMORED_KRYPT("#F97316", "#38BDF8", 0.82, 1.65);

        public final String hexMain;
        public final String hexCore;
        public final double speedMult;
        public final double hpMult;

        StrainType(String hexMain, String hexCore, double speedMult, double hpMult) {
            this.hexMain = hexMain;
            this.hexCore = hexCore;
            this.speedMult = speedMult;
            this.hpMult = hpMult;
        }
    }

    public Pane pane = new Pane();
    public Scene scene = new Scene(pane, SCREEN_WIDTH, SCREEN_HEIGHT);
    public Canvas bgCanvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
    public GraphicsContext gc = bgCanvas.getGraphicsContext2D();
    public Random random = new Random();

    // Réutilise le moteur audio de HelloApplication
    public HelloApplication.CyberAudioEngine audio = new HelloApplication.CyberAudioEngine();
    private Runnable onBackToMainCallback = null;

    // Environnement : false = FIBRE OPTIQUE (Cyber-Câble), true = FLUX SANGUIN (Artère Bio)
    public boolean bioArteryMode = true;

    // Spécialisation de la Nano-Sonde : 0 = Titan-Bio, 1 = Hyper-Plasma, 2 = Swarm-Tech
    public int selectedDeck = 1;

    // Alternance de style : false = Futuriste (Impair), true = Bande Dessinée (Pair)
    public boolean comicMode = false;

    public ScreenState currentScreen = ScreenState.ACCUEIL;
    public double time = 0;

    // Contrôles Clavier & Souris
    public final Set<KeyCode> activeKeys = new HashSet<>();
    public double mouseX = SCREEN_WIDTH / 2.0, mouseY = SCREEN_HEIGHT / 2.0;
    public boolean isShootingHold = false;
    public boolean isVacuumHold = false;
    public int selectedWeapon = 1; // 1: Plasma, 2: Dispersion, 3: Onde Choc, 4: Vortex+, 5: Mine, 6: Essaim

    // La Nano-Sonde (Joueur)
    public NanoProbe player = new NanoProbe(SCREEN_WIDTH / 2.0, SCREEN_HEIGHT / 2.0);

    // Statistiques évolutives & Boutique
    public double maxHull = 100.0;
    public double hull = 100.0;
    public double maxPlasmaEnergy = 150.0;
    public double plasmaEnergy = 150.0;
    public double plasmaRegen = 42.0;

    public int upgWeaponLevel = 1;
    public int upgVacuumLevel = 1;
    public int upgThrusterLevel = 1;
    public int droneCount = 0;

    // Systèmes Tactiques
    public int stasisCharges = 2; // [R] Stase & Répulsion Anti-Mitose
    public double stasisFreezeTimer = 0;
    public double hyperSurgeCharge = 0;
    public double hyperSurgeTimer = 0;
    public static final double HYPER_SURGE_COOLDOWN = 16.0;

    // Bouton Rouge [ESPACE] : Décharge Nova 360°
    public double novaCharge = 50.0;
    public boolean novaWaveActive = false;
    public double novaWaveRadius = 0;

    // Score, Vagues, Rangs & Médailles
    public int score = 0;
    public int credits = 250;
    public int wave = 1;
    public int totalSplits = 0;
    public int microAspiratedCount = 0;
    public int microDisintegratedCount = 0;
    public int totalMedalsEarned = 0;
    public int combo = 0;
    public double comboTimer = 0;
    public double doubleXpTimer = 0;
    public int currentRankIndex = 0;

    public String activeMedalBanner = "";
    public double medalBannerTimer = 0;
    public int popupSideToggle = 0;

    // Effets Caméra
    public double shakeIntensity = 0;
    public double screenFlashAlpha = 0;
    public Color screenFlashColor = Color.web("#05D5FA");
    public boolean roundEnding = false;
    public double victoryDelayTimer = 0;

    // Flux artériel / optique
    public double pulseBeatTimer = 0;
    public double spawnTimer = 0;
    public int macroRemainingToSpawn = 0;

    // Entités du Jeu
    public List<ViralCell> viruses = new ArrayList<>();
    public List<PlasmaBolt> bolts = new ArrayList<>();
    public List<BioMine> mines = new ArrayList<>();
    public List<HomingNanite> naniteSwarm = new ArrayList<>();
    public List<OrbitalDrone> drones = new ArrayList<>();
    public List<StreamParticle> backgroundStream = new ArrayList<>();
    public List<Particle> particles = new ArrayList<>();
    public List<Shockwave> shockwaves = new ArrayList<>();
    public List<CyberLaser> lasers = new ArrayList<>();
    public List<FloatingText> floatingTexts = new ArrayList<>();

    public static final String[] RANK_NAMES = {
            "SONDE AUXILIAIRE",
            "PURGEUR CELLULaire",
            "VECTEUR PLASMA OR",
            "MAÎTRE DE LA MITOSE",
            "ARCHONTE NANO-OMÉGA"
    };
    public static final int[] RANK_THRESHOLDS = {0, 3000, 7500, 15000, 26000};

    public static final String[] COMIC_WORDS = {"SLURP!", "ZAP!", "MITOSE!", "SPLASH!", "KAPOW!", "BZZZT!", "POW!"};
    public static final String[] FUTURISTIC_WORDS = {"PURGED!", "ABSORBED!", "CLEAVAGE!", "PLASMA_HIT!", "VORTEX!", "CLEANSED!"};

    /**
     * Permet de lancer Nano-Sonde directement depuis HelloApplication dans le même Stage
     */
    public static void launchInStage(Stage stage, Runnable onBackToMain) {
        NanoSondeGame game = new NanoSondeGame();
        game.onBackToMainCallback = onBackToMain;
        try {
            game.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        bgCanvas.widthProperty().bind(scene.widthProperty());
        bgCanvas.heightProperty().bind(scene.heightProperty());

        audio.startEngine();
        setupControls(stage);
        updateLayoutMetrics();
        initBackgroundStream();
        loadNewScreen(ScreenState.ACCUEIL);

        AnimationTimer timer = new AnimationTimer() {
            long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double dt = Math.min(0.05, (now - lastTime) * 1e-9);
                lastTime = now;

                updateLayoutMetrics();

                if (currentScreen == ScreenState.GAMEPLAY) {
                    updateGameplay(dt);
                    renderGame();
                }
            }
        };
        timer.start();

        stage.setTitle("NANO-SONDE // [FLUX SANGUIN & FIBRE OPTIQUE : SHOOT'EM UP 360° & MITOSE]");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setOnCloseRequest(e -> audio.stopEngine());
        stage.show();
    }

    public void updateLayoutMetrics() {
        double w = Math.max(900, scene.getWidth());
        double h = Math.max(620, scene.getHeight());

        arenaW = Math.max(480, Math.min(620, w - 570));
        arenaH = Math.max(420, Math.min(520, h - 220));
        arenaX = (w - arenaW) / 2.0;
        arenaY = (h - arenaH) / 2.0 + 6;
    }

    private void initBackgroundStream() {
        backgroundStream.clear();
        for (int i = 0; i < 45; i++) {
            backgroundStream.add(new StreamParticle(
                    arenaX + random.nextDouble() * arenaW,
                    arenaY + random.nextDouble() * arenaH,
                    40 + random.nextDouble() * 95,
                    4 + random.nextDouble() * 9
            ));
        }
    }

    // =========================================================================
    // BOUCLE PRINCIPALE DU GAMEPLAY (PHYSIQUE VECTORIELLE, MITOSE, VORTEX)
    // =========================================================================

    private void updateGameplay(double dt) {
        time += dt;
        pulseBeatTimer += dt;

        // Régénération d'énergie plasma
        plasmaEnergy = Math.min(maxPlasmaEnergy, plasmaEnergy + plasmaRegen * dt);

        // Décompte des timers
        if (comboTimer > 0) {
            comboTimer -= dt;
            if (comboTimer <= 0) combo = 0;
        }
        if (doubleXpTimer > 0) doubleXpTimer = Math.max(0, doubleXpTimer - dt);
        if (stasisFreezeTimer > 0) stasisFreezeTimer = Math.max(0, stasisFreezeTimer - dt);
        if (medalBannerTimer > 0) medalBannerTimer = Math.max(0, medalBannerTimer - dt);
        if (shakeIntensity > 0) shakeIntensity = Math.max(0, shakeIntensity - dt * 28.0);
        if (screenFlashAlpha > 0) screenFlashAlpha = Math.max(0, screenFlashAlpha - dt * 2.5);

        updateStreamParticles(dt);
        updateHyperSurge(dt);
        updateNovaSweep(dt);
        updatePlayerProbe(dt);
        updateDronesAndNanites(dt);
        updatePlasmaBoltsAndMines(dt);
        updateVirusesAndMitosis(dt);
        updateVisualEffects(dt);

        // Apparition progressive des Macro-Virus de la vague
        if (!roundEnding && macroRemainingToSpawn > 0 && stasisFreezeTimer <= 0) {
            spawnTimer -= dt;
            if (spawnTimer <= 0) {
                spawnMacroVirusOnEdge();
                macroRemainingToSpawn--;
                spawnTimer = Math.max(1.6, 3.8 - wave * 0.25);
            }
        }

        // Vérification de fin de vague
        if (roundEnding) {
            victoryDelayTimer -= dt;
            if (victoryDelayTimer <= 0) {
                roundEnding = false;
                wave++;
                stasisCharges++;
                loadNewScreen(ScreenState.SHOP);
            }
        } else if (macroRemainingToSpawn == 0 && viruses.isEmpty()) {
            triggerWaveVictory();
        }
    }

    private void updateStreamParticles(double dt) {
        // En mode Flux Sanguin, le battement cardiaque crée une poussée systolique périodique
        double systolicBoost = bioArteryMode ? (1.0 + 0.55 * Math.max(0, Math.sin(time * 5.5))) : 1.35;

        for (StreamParticle sp : backgroundStream) {
            sp.x += sp.speed * systolicBoost * dt;
            sp.y += Math.sin(time * 2.5 + sp.phase) * 12.0 * dt;
            if (sp.x > arenaX + arenaW + 15) {
                sp.x = arenaX - 15;
                sp.y = arenaY + 18 + random.nextDouble() * (arenaH - 36);
            }
        }
    }

    private void updatePlayerProbe(double dt) {
        // Orientation 360° fluide vers la souris
        player.angle = Math.atan2(mouseY - player.y, mouseX - player.x);

        // Vecteur d'accélération clavier WASD / ZQSD / Flèches
        double ax = 0, ay = 0;
        if (activeKeys.contains(KeyCode.W) || activeKeys.contains(KeyCode.Z) || activeKeys.contains(KeyCode.UP)) ay -= 1;
        if (activeKeys.contains(KeyCode.S) || activeKeys.contains(KeyCode.DOWN)) ay += 1;
        if (activeKeys.contains(KeyCode.A) || activeKeys.contains(KeyCode.Q) || activeKeys.contains(KeyCode.LEFT)) ax -= 1;
        if (activeKeys.contains(KeyCode.D) || activeKeys.contains(KeyCode.RIGHT)) ax += 1;

        double len = Math.hypot(ax, ay);
        double thrustAccel = 740.0 + (upgThrusterLevel - 1) * 110.0 + (selectedDeck == 1 ? 85.0 : 0);

        if (len > 0) {
            ax = (ax / len) * thrustAccel;
            ay = (ay / len) * thrustAccel;

            // Particules de propulsion à l'arrière du mouvement
            if (random.nextDouble() < 0.75) {
                double pvx = -ax * 0.18 + (random.nextDouble() - 0.5) * 60;
                double pvy = -ay * 0.18 + (random.nextDouble() - 0.5) * 60;
                Color thrustColor = hyperSurgeTimer > 0 ? Color.web("#FACC15") : Color.web("#05D5FA");
                particles.add(new Particle(player.x, player.y, pvx, pvy, 5.0, thrustColor, false));
            }
        }

        // Léger courant artériel en mode Flux Sanguin
        if (bioArteryMode) {
            double beatPush = Math.max(0, Math.sin(time * 5.5)) * 45.0;
            ax += beatPush;
        }

        // Intégration Vélocité & Inertie fluide
        player.vx += ax * dt;
        player.vy += ay * dt;

        double damping = Math.pow(0.045, dt); // Friction fluide style Geometry Wars / Asteroids
        player.vx *= damping;
        player.vy *= damping;

        player.x += player.vx * dt;
        player.y += player.vy * dt;

        // Rebonds élastiques sur les parois de l'artère / fibre optique
        double margin = player.radius + 8;
        if (player.x < arenaX + margin) {
            player.x = arenaX + margin;
            player.vx = Math.abs(player.vx) * 0.65;
        } else if (player.x > arenaX + arenaW - margin) {
            player.x = arenaX + arenaW - margin;
            player.vx = -Math.abs(player.vx) * 0.65;
        }
        if (player.y < arenaY + margin) {
            player.y = arenaY + margin;
            player.vy = Math.abs(player.vy) * 0.65;
        } else if (player.y > arenaY + arenaH - margin) {
            player.y = arenaY + arenaH - margin;
            player.vy = -Math.abs(player.vy) * 0.65;
        }

        if (player.invulnerableTimer > 0) {
            player.invulnerableTimer = Math.max(0, player.invulnerableTimer - dt);
        }
        if (player.dashCooldown > 0) {
            player.dashCooldown = Math.max(0, player.dashCooldown - dt);
        }

        // Tir au Canon Plasma (Clic Gauche maintenu)
        player.fireCooldown = Math.max(0, player.fireCooldown - dt);
        boolean vacuumActiveNow = isVacuumHold || activeKeys.contains(KeyCode.E) || (selectedWeapon == 4 && isShootingHold);

        if (isShootingHold && !vacuumActiveNow && player.fireCooldown <= 0) {
            fireSelectedWeapon();
        }

        // Vortex d'Aspiration Magnétique (Clic Droit maintenu ou touche [E])
        player.vacuumActive = vacuumActiveNow;
        if (player.vacuumActive) {
            processMagneticVacuum(dt);
        }
    }

    // =========================================================================
    // SYSTÈME DE TIR 360° & VORTEX D'ASPIRATION DES MICRO-VIRUS
    // =========================================================================

    private void fireSelectedWeapon() {
        boolean freeSurge = (hyperSurgeTimer > 0);

        if (selectedWeapon == 2) {
            // [2] Canon Dispersant 360° (5 projectiles en éventail)
            double cost = freeSurge ? 0 : 12.0;
            if (plasmaEnergy >= cost) {
                plasmaEnergy -= cost;
                player.fireCooldown = freeSurge ? 0.12 : 0.22;
                audio.playLaser();
                int count = 5 + (upgWeaponLevel - 1);
                for (int i = 0; i < count; i++) {
                    double spread = (i - (count - 1) / 2.0) * 0.14;
                    spawnBolt(player.angle + spread, 640.0, 1.35, Color.web("#FACC15"), ! bioArteryMode || selectedDeck == 1);
                }
            }
            return;
        }

        if (selectedWeapon == 3) {
            // [3] Onde de Choc Répulsive 360°
            double cost = freeSurge ? 0 : 28.0;
            if (plasmaEnergy >= cost) {
                plasmaEnergy -= cost;
                player.fireCooldown = 0.55;
                triggerRadialPulseBlast();
            }
            return;
        }

        if (selectedWeapon == 5) {
            // [5] Largage de Mine Bio-Plasma
            double cost = freeSurge ? 0 : 24.0;
            if (plasmaEnergy >= cost && mines.size() < 8) {
                plasmaEnergy -= cost;
                player.fireCooldown = 0.40;
                audio.playPowerup();
                mines.add(new BioMine(player.x, player.y));
                spawnPopup(player.x, player.y, "💣 MINE BIO-PLASMA POSÉE !", Color.web("#D946EF"), 14, PopupStyle.HEX_TECH);
            }
            return;
        }

        if (selectedWeapon == 6) {
            // [6] Essaim de Nano-Missiles Guidés
            double cost = freeSurge ? 0 : 26.0;
            if (plasmaEnergy >= cost) {
                plasmaEnergy -= cost;
                player.fireCooldown = 0.42;
                audio.playLaser();
                for (int i = 0; i < 4; i++) {
                    double ang = player.angle + (i - 1.5) * 0.4;
                    naniteSwarm.add(new HomingNanite(player.x, player.y, ang));
                }
            }
            return;
        }

        // [1] Canon Plasma 360° Rapide (par défaut)
        double baseInterval = (selectedDeck == 1) ? 0.085 : 0.115;
        player.fireCooldown = freeSurge ? baseInterval * 0.65 : baseInterval;
        double cost = freeSurge ? 0 : 3.2;

        if (plasmaEnergy >= cost) {
            plasmaEnergy -= cost;
            audio.playLaser();

            boolean bounce = (!bioArteryMode) || (selectedDeck == 1);
            Color boltColor = freeSurge ? Color.web("#FACC15") : (bioArteryMode ? Color.web("#00FF9D") : Color.web("#05D5FA"));

            if (upgWeaponLevel == 1 && !freeSurge) {
                spawnBolt(player.angle, 720.0, 1.2, boltColor, bounce);
            } else if (upgWeaponLevel == 2) {
                spawnBolt(player.angle - 0.06, 740.0, 1.2, boltColor, bounce);
                spawnBolt(player.angle + 0.06, 740.0, 1.2, boltColor, bounce);
            } else {
                spawnBolt(player.angle - 0.10, 760.0, 1.3, boltColor, bounce);
                spawnBolt(player.angle, 790.0, 1.5, Color.WHITE, bounce);
                spawnBolt(player.angle + 0.10, 760.0, 1.3, boltColor, bounce);
            }

            // Recul léger pour le feeling arcade
            player.vx -= Math.cos(player.angle) * 18.0;
            player.vy -= Math.sin(player.angle) * 18.0;
        }
    }

    private void spawnBolt(double angle, double speed, double dmg, Color color, boolean canBounce) {
        double noseX = player.x + Math.cos(angle) * (player.radius + 6);
        double noseY = player.y + Math.sin(angle) * (player.radius + 6);
        bolts.add(new PlasmaBolt(noseX, noseY, Math.cos(angle) * speed, Math.sin(angle) * speed, dmg, color, canBounce ? 1 : 0));
    }

    private void triggerRadialPulseBlast() {
        audio.playNuke();
        double blastRadius = 155.0 + upgVacuumLevel * 20.0;
        shockwaves.add(new Shockwave(player.x, player.y, blastRadius, Color.web("#38BDF8")));
        triggerScreenShake(12.0, Color.web("#38BDF8"), 0.25);

        List<ViralCell> spawnedFromSplit = new ArrayList<>();
        Iterator<ViralCell> it = viruses.iterator();
        while (it.hasNext()) {
            ViralCell vc = it.next();
            double dist = Math.hypot(vc.x - player.x, vc.y - player.y);
            if (dist <= blastRadius + vc.radius) {
                double pushAng = Math.atan2(vc.y - player.y, vc.x - player.x);
                vc.vx += Math.cos(pushAng) * 380.0;
                vc.vy += Math.sin(pushAng) * 380.0;
                vc.hp -= 2.2;
                if (vc.hp <= 0) {
                    it.remove();
                    handleVirusDestructionAndMitosis(vc, spawnedFromSplit, false);
                }
            }
        }
        viruses.addAll(spawnedFromSplit);
    }

    /**
     * VORTEX D'ASPIRATION MAGNÉTIQUE :
     * Attire et absorbe les Micro-Virus (Stade 1) pour soigner la sonde et recharger l'énergie,
     * tout en ralentissant les virus moyens/géants dans le cône.
     */
    private void processMagneticVacuum(double dt) {
        double maxRange = 195.0 + (upgVacuumLevel - 1) * 42.0 + (selectedDeck == 0 ? 45.0 : 0);
        double coneHalfAngle = Math.toRadians(44.0 + (upgVacuumLevel - 1) * 7.0);

        // Particules d'aspiration convergeant vers la sonde
        if (random.nextDouble() < 0.85) {
            double randAng = player.angle + (random.nextDouble() - 0.5) * coneHalfAngle * 1.8;
            double randDist = 60 + random.nextDouble() * (maxRange - 60);
            double px = player.x + Math.cos(randAng) * randDist;
            double py = player.y + Math.sin(randAng) * randDist;
            double vx = (player.x - px) * 3.2;
            double vy = (player.y - py) * 3.2;
            particles.add(new Particle(px, py, vx, vy, 4.0, Color.web("#00FF9D"), true));
        }

        Iterator<ViralCell> it = viruses.iterator();
        while (it.hasNext()) {
            ViralCell v = it.next();
            double dx = v.x - player.x;
            double dy = v.y - player.y;
            double dist = Math.hypot(dx, dy);

            if (dist <= maxRange) {
                double angleToVirus = Math.atan2(dy, dx);
                double angleDiff = Math.abs(normalizeAngle(angleToVirus - player.angle));

                if (angleDiff <= coneHalfAngle || dist <= player.radius + 34) {
                    if (v.stage == 1) {
                        // C'est un MICRO-VIRUS : Aspiration haute vitesse vers le cœur de la Nano-Sonde !
                        double pullStrength = 980.0 + upgVacuumLevel * 180.0;
                        v.vx += (-dx / Math.max(1, dist)) * pullStrength * dt;
                        v.vy += (-dy / Math.max(1, dist)) * pullStrength * dt;
                        v.vx *= 0.90;
                        v.vy *= 0.90;

                        if (dist <= player.radius + v.radius + 14) {
                            // MICRO-VIRUS ASPIRÉ !
                            it.remove();
                            microAspiratedCount++;
                            hull = Math.min(maxHull, hull + 3.5);
                            plasmaEnergy = Math.min(maxPlasmaEnergy, plasmaEnergy + 14.0);
                            novaCharge = Math.min(100.0, novaCharge + 4.5);

                            combo++;
                            comboTimer = 4.0;
                            audio.playPowerup();
                            spawnExplosion(v.x, v.y, Color.web("#00FF9D"), 14, 150);

                            int pts = 95 * Math.min(6, Math.max(1, combo));
                            addScore(pts, 18, v.x, v.y, "🧲 ASPIRÉ +" + pts + " XP", Color.web("#00FF9D"), 13, PopupStyle.HEX_TECH);

                            if (microAspiratedCount == 20) {
                                awardMedal("🧲 MÉDAILLE [ASPIRATEUR QUANTIQUE] : 20 MICRO-VIRUS ABSORBÉS !", 700, 220);
                            }
                        }
                    } else {
                        // Stade 2 ou 3 : Le vortex ralentit leur charge et érode légèrement leur membrane
                        v.vx *= (1.0 - 1.5 * dt);
                        v.vy *= (1.0 - 1.5 * dt);
                        v.hp -= dt * (0.8 + upgVacuumLevel * 0.35);
                    }
                }
            }
        }
    }

    private double normalizeAngle(double a) {
        while (a > Math.PI) a -= Math.PI * 2;
        while (a < -Math.PI) a += Math.PI * 2;
        return a;
    }

    // =========================================================================
    // LOGIQUE DES VIRUS & MÉCANIQUE DE MITOSE (1 -> 2 MOYENS -> 4 MICRO-VIRUS)
    // =========================================================================

    private void spawnMacroVirusOnEdge() {
        double x, y;
        int side = random.nextInt(4);
        if (side == 0) { // Gauche
            x = arenaX + 38;
            y = arenaY + 40 + random.nextDouble() * (arenaH - 80);
        } else if (side == 1) { // Droite
            x = arenaX + arenaW - 38;
            y = arenaY + 40 + random.nextDouble() * (arenaH - 80);
        } else if (side == 2) { // Haut
            x = arenaX + 40 + random.nextDouble() * (arenaW - 80);
            y = arenaY + 38;
        } else { // Bas
            x = arenaX + 40 + random.nextDouble() * (arenaW - 80);
            y = arenaY + arenaH - 38;
        }

        // Évite de faire apparaître un Macro-Virus directement sur le joueur
        if (Math.hypot(x - player.x, y - player.y) < 130) {
            x = (player.x < arenaX + arenaW / 2) ? arenaX + arenaW - 45 : arenaX + 45;
        }

        StrainType strain = pickWaveStrain();
        ViralCell macro = new ViralCell(x, y, 3, strain, wave);
        double ang = Math.atan2(player.y - y, player.x - x) + (random.nextDouble() - 0.5) * 0.6;
        double spd = 68.0 * strain.speedMult;
        macro.vx = Math.cos(ang) * spd;
        macro.vy = Math.sin(ang) * spd;

        viruses.add(macro);
        shockwaves.add(new Shockwave(x, y, 52, Color.web(strain.hexMain)));
    }

    private StrainType pickWaveStrain() {
        double r = random.nextDouble();
        if (wave == 1) return (r < 0.78) ? StrainType.ALPHA_CARMINE : StrainType.SYNAPSE_VOLT;
        if (r < 0.50) return StrainType.ALPHA_CARMINE;
        if (r < 0.80) return StrainType.SYNAPSE_VOLT;
        return StrainType.ARMORED_KRYPT;
    }

    private void updateVirusesAndMitosis(double dt) {
        List<ViralCell> newlyDivided = new ArrayList<>();
        Iterator<ViralCell> it = viruses.iterator();

        while (it.hasNext()) {
            ViralCell v = it.next();

            // Si un virus a été détruit par l'érosion du vortex
            if (v.hp <= 0) {
                it.remove();
                handleVirusDestructionAndMitosis(v, newlyDivided, false);
                continue;
            }

            v.wobblePhase += dt * (4.0 + (4 - v.stage) * 2.5);

            if (stasisFreezeTimer <= 0) {
                // Guidage vers la Nano-Sonde
                double dx = player.x - v.x;
                double dy = player.y - v.y;
                double dist = Math.max(1.0, Math.hypot(dx, dy));

                double trackingStrength = switch (v.stage) {
                    case 3 -> 48.0;  // Macro-Virus : dérive lourde + charge
                    case 2 -> 115.0; // Méso-Virus : traque active
                    default -> 240.0; // Micro-Virus : ultra-rapide et agressif !
                };
                trackingStrength *= v.strain.speedMult * (1.0 + (wave - 1) * 0.08);

                v.vx += (dx / dist) * trackingStrength * dt;
                v.vy += (dy / dist) * trackingStrength * dt;

                // Limitation de vitesse selon le stade de mitose
                double maxSpd = switch (v.stage) {
                    case 3 -> 110.0;
                    case 2 -> 195.0;
                    default -> 320.0;
                } * v.strain.speedMult;

                double curSpd = Math.hypot(v.vx, v.vy);
                if (curSpd > maxSpd && !player.vacuumActive) {
                    v.vx = (v.vx / curSpd) * maxSpd;
                    v.vy = (v.vy / curSpd) * maxSpd;
                }

                v.x += v.vx * dt;
                v.y += v.vy * dt;
            }

            // Rebond sur les parois de l'artère / fibre optique
            if (v.x - v.radius < arenaX + 4) {
                v.x = arenaX + 4 + v.radius;
                v.vx = Math.abs(v.vx);
            } else if (v.x + v.radius > arenaX + arenaW - 4) {
                v.x = arenaX + arenaW - 4 - v.radius;
                v.vx = -Math.abs(v.vx);
            }
            if (v.y - v.radius < arenaY + 4) {
                v.y = arenaY + 4 + v.radius;
                v.vy = Math.abs(v.vy);
            } else if (v.y + v.radius > arenaY + arenaH - 4) {
                v.y = arenaY + arenaH - 4 - v.radius;
                v.vy = -Math.abs(v.vy);
            }

            // Collision avec la Nano-Sonde
            double distToPlayer = Math.hypot(v.x - player.x, v.y - player.y);
            if (distToPlayer < v.radius + player.radius) {
                // Si le Vortex est actif et que c'est un Micro-Virus (Stade 1), on l'absorbe sans subir de dégâts !
                if (v.stage == 1 && player.vacuumActive) {
                    it.remove();
                    microAspiratedCount++;
                    hull = Math.min(maxHull, hull + 3.5);
                    plasmaEnergy = Math.min(maxPlasmaEnergy, plasmaEnergy + 12.0);
                    audio.playPowerup();
                    spawnExplosion(v.x, v.y, Color.web("#00FF9D"), 12, 140);
                    addScore(110, 18, v.x, v.y, "🧲 ASPIRÉ +110 XP", Color.web("#00FF9D"), 13, PopupStyle.HEX_TECH);
                    continue;
                }

                // Sinon impact contre la coque
                if (player.invulnerableTimer <= 0) {
                    double dmg = (v.stage == 3) ? 22.0 : (v.stage == 2 ? 14.0 : 8.0);
                    hull -= dmg;
                    player.invulnerableTimer = 0.65;
                    audio.playExplosion();
                    triggerScreenShake(16.0, Color.web("#FF2A6D"), 0.38);
                    spawnPopup(player.x, player.y, "⚠️ IMPACT COQUE -" + (int) dmg + " PV !", Color.web("#FF2A6D"), 15, PopupStyle.WARNING_SHIELD);

                    // Rebond de choc
                    double pushAngle = Math.atan2(player.y - v.y, player.x - v.x);
                    player.vx += Math.cos(pushAngle) * 280.0;
                    player.vy += Math.sin(pushAngle) * 280.0;
                    v.vx = -Math.cos(pushAngle) * 220.0;
                    v.vy = -Math.sin(pushAngle) * 220.0;

                    if (hull <= 0) {
                        hull = 0;
                        audio.playGameOver();
                        loadNewScreen(ScreenState.GAME_OVER);
                        return;
                    }
                }
            }
        }

        viruses.addAll(newlyDivided);
    }

    /**
     * CŒUR DE LA MÉCANIQUE DE MITOSE :
     * - Stade 3 (Gros Virus) -> Se divise en 2 Virus Moyens (Stade 2)
     * - Stade 2 (Virus Moyen) -> Se divise en 4 Micro-Virus ultra-rapides (Stade 1)
     * - Stade 1 (Micro-Virus) -> Désintégré au Canon Plasma !
     */
    private void handleVirusDestructionAndMitosis(ViralCell parent, List<ViralCell> outList, boolean vaporizeWithoutSplit) {
        Color c = Color.web(parent.strain.hexMain);
        spawnExplosion(parent.x, parent.y, c, 24, 240);
        shockwaves.add(new Shockwave(parent.x, parent.y, parent.radius * 2.4, c));
        audio.playExplosion();

        combo++;
        comboTimer = 4.0;
        novaCharge = Math.min(100.0, novaCharge + 5.5);

        if (combo == 5 || (totalSplits > 0 && totalSplits % 8 == 0)) {
            doubleXpTimer = 7.0;
        }

        if (!vaporizeWithoutSplit && parent.stage == 3) {
            // MITOSE 1 : 1 GROS VIRUS -> 2 VIRUS MOYENS (STADE 2)
            totalSplits++;
            double baseAngle = Math.atan2(parent.vy, parent.vx);
            for (int i = 0; i < 2; i++) {
                double splitAngle = baseAngle + (i == 0 ? Math.PI / 2.2 : -Math.PI / 2.2) + (random.nextDouble() - 0.5) * 0.3;
                double offset = 18.0;
                ViralCell child = new ViralCell(
                        parent.x + Math.cos(splitAngle) * offset,
                        parent.y + Math.sin(splitAngle) * offset,
                        2,
                        parent.strain,
                        wave
                );
                double childSpeed = 165.0 * parent.strain.speedMult;
                child.vx = Math.cos(splitAngle) * childSpeed;
                child.vy = Math.sin(splitAngle) * childSpeed;
                outList.add(child);
            }
            lasers.add(new CyberLaser(parent.x - 24, parent.y, parent.x + 24, parent.y, Color.web("#FACC15"), 4.0));
            addScore(180, 28, parent.x, parent.y, "🧬 MITOSE x2 ! +180 XP", Color.web("#FACC15"), 15, PopupStyle.SPEECH_BUBBLE);

        } else if (!vaporizeWithoutSplit && parent.stage == 2) {
            // MITOSE 2 : 1 VIRUS MOYEN -> 4 MICRO-VIRUS ULTRA-RAPIDES (STADE 1)
            totalSplits++;
            for (int i = 0; i < 4; i++) {
                double splitAngle = (i * Math.PI / 2.0) + random.nextDouble() * 0.45;
                double offset = 14.0;
                ViralCell micro = new ViralCell(
                        parent.x + Math.cos(splitAngle) * offset,
                        parent.y + Math.sin(splitAngle) * offset,
                        1,
                        parent.strain,
                        wave
                );
                double microSpeed = 265.0 * parent.strain.speedMult;
                micro.vx = Math.cos(splitAngle) * microSpeed;
                micro.vy = Math.sin(splitAngle) * microSpeed;
                outList.add(micro);
            }
            addScore(240, 35, parent.x, parent.y, "🦠 4 MICRO-VIRUS ! ASPIRE-LES [CLIC DROIT] !", Color.web("#FF2A6D"), 14, PopupStyle.STARBURST);

        } else {
            // STADE 1 DÉSINTÉGRÉ AU CANON PLASMA (ou vaporisé par Nova)
            microDisintegratedCount++;
            String word = comicMode ? COMIC_WORDS[random.nextInt(COMIC_WORDS.length)] : FUTURISTIC_WORDS[random.nextInt(FUTURISTIC_WORDS.length)];
            int pts = 85 * Math.min(6, Math.max(1, combo));
            addScore(pts, 14, parent.x, parent.y, "⚡ " + word + " +" + pts + " XP", Color.web("#38BDF8"), 13, PopupStyle.NEON_FLOAT);

            if (microDisintegratedCount == 25) {
                awardMedal("🔫 MÉDAILLE [TIREUR PLASMA] : 25 MICRO-VIRUS DÉSINTÉGRÉS !", 650, 200);
            }
        }
    }

    private void updatePlasmaBoltsAndMines(double dt) {
        List<ViralCell> newlySpawned = new ArrayList<>();

        // 1. Mise à jour des tirs Plasma
        Iterator<PlasmaBolt> bIt = bolts.iterator();
        while (bIt.hasNext()) {
            PlasmaBolt b = bIt.next();
            b.update(dt);

            // Rebond ou destruction sur les parois
            boolean hitWall = false;
            if (b.x < arenaX + 4 || b.x > arenaX + arenaW - 4) {
                if (b.bouncesLeft > 0) {
                    b.vx = -b.vx;
                    b.bouncesLeft--;
                    b.x = Math.max(arenaX + 5, Math.min(arenaX + arenaW - 5, b.x));
                } else hitWall = true;
            }
            if (b.y < arenaY + 4 || b.y > arenaY + arenaH - 4) {
                if (b.bouncesLeft > 0) {
                    b.vy = -b.vy;
                    b.bouncesLeft--;
                    b.y = Math.max(arenaY + 5, Math.min(arenaY + arenaH - 5, b.y));
                } else hitWall = true;
            }

            if (hitWall || b.life <= 0) {
                bIt.remove();
                continue;
            }

            // Collision tir Plasma <-> Virus
            Iterator<ViralCell> vIt = viruses.iterator();
            while (vIt.hasNext()) {
                ViralCell v = vIt.next();
                if (Math.hypot(b.x - v.x, b.y - v.y) <= v.radius + b.radius) {
                    v.hp -= b.damage;
                    v.hitFlash = 1.0;
                    particles.add(new Particle(b.x, b.y, (random.nextDouble() - 0.5) * 90, (random.nextDouble() - 0.5) * 90, 4.5, b.color, false));
                    bIt.remove();

                    if (v.hp <= 0) {
                        vIt.remove();
                        handleVirusDestructionAndMitosis(v, newlySpawned, false);
                    }
                    break;
                }
            }
        }

        // 2. Mise à jour des Mines Bio-Plasma
        Iterator<BioMine> mIt = mines.iterator();
        while (mIt.hasNext()) {
            BioMine m = mIt.next();
            m.pulse += dt * 6.0;

            boolean triggered = false;
            for (ViralCell v : viruses) {
                if (Math.hypot(v.x - m.x, v.y - m.y) <= v.radius + 24.0) {
                    triggered = true;
                    break;
                }
            }

            if (triggered) {
                mIt.remove();
                audio.playNuke();
                triggerScreenShake(15.0, Color.web("#D946EF"), 0.32);
                shockwaves.add(new Shockwave(m.x, m.y, 130, Color.web("#D946EF")));
                spawnExplosion(m.x, m.y, Color.web("#D946EF"), 38, 280);

                Iterator<ViralCell> vIt = viruses.iterator();
                while (vIt.hasNext()) {
                    ViralCell v = vIt.next();
                    if (Math.hypot(v.x - m.x, v.y - m.y) <= 130.0) {
                        v.hp -= 4.5;
                        if (v.hp <= 0) {
                            vIt.remove();
                            handleVirusDestructionAndMitosis(v, newlySpawned, false);
                        }
                    }
                }
            }
        }

        viruses.addAll(newlySpawned);
    }

    private void updateDronesAndNanites(double dt) {
        List<ViralCell> newlySpawned = new ArrayList<>();

        // Drones Orbitaux Autonomes
        for (int i = 0; i < drones.size(); i++) {
            OrbitalDrone d = drones.get(i);
            d.update(dt, player.x, player.y, 54.0);

            if (d.fireCooldown <= 0 && !viruses.isEmpty()) {
                ViralCell closest = null;
                double minDist = 230.0;
                for (ViralCell v : viruses) {
                    double dist = Math.hypot(v.x - d.x, v.y - d.y);
                    if (dist < minDist) {
                        minDist = dist;
                        closest = v;
                    }
                }
                if (closest != null) {
                    d.fireCooldown = (selectedDeck == 2) ? 0.55 : 0.80;
                    lasers.add(new CyberLaser(d.x, d.y, closest.x, closest.y, Color.web("#10B981"), 3.2));
                    closest.hp -= 1.4;
                    closest.hitFlash = 1.0;
                    if (closest.hp <= 0) {
                        viruses.remove(closest);
                        handleVirusDestructionAndMitosis(closest, newlySpawned, false);
                    }
                }
            }
        }

        // Nano-Missiles Guidés
        Iterator<HomingNanite> nIt = naniteSwarm.iterator();
        while (nIt.hasNext()) {
            HomingNanite hn = nIt.next();
            ViralCell target = null;
            double bestDist = 9999;
            for (ViralCell v : viruses) {
                double d = Math.hypot(v.x - hn.x, v.y - hn.y);
                if (d < bestDist) {
                    bestDist = d;
                    target = v;
                }
            }

            hn.update(dt, target);
            if (random.nextDouble() < 0.6) {
                particles.add(new Particle(hn.x, hn.y, 0, 0, 3.2, Color.web("#FACC15"), true));
            }

            if (hn.life <= 0) {
                nIt.remove();
                continue;
            }

            if (target != null && bestDist <= target.radius + 10.0) {
                nIt.remove();
                target.hp -= 2.8;
                target.hitFlash = 1.0;
                spawnExplosion(hn.x, hn.y, Color.web("#FACC15"), 14, 160);
                if (target.hp <= 0) {
                    viruses.remove(target);
                    handleVirusDestructionAndMitosis(target, newlySpawned, false);
                }
            }
        }

        viruses.addAll(newlySpawned);
    }

    // =========================================================================
    // CAPACITÉS SPÉCIALES : SURCHARGE, NOVA [ESPACE] & STASE QUANTIQUE [R]
    // =========================================================================

    private void updateHyperSurge(double dt) {
        if (roundEnding) return;
        if (hyperSurgeTimer > 0) {
            hyperSurgeTimer = Math.max(0, hyperSurgeTimer - dt);
            plasmaEnergy = maxPlasmaEnergy;
        } else {
            hyperSurgeCharge += dt;
            if (hyperSurgeCharge >= HYPER_SURGE_COOLDOWN) {
                hyperSurgeCharge = 0;
                hyperSurgeTimer = 5.5;
                audio.playPowerup();
                triggerScreenShake(12.0, Color.web("#FACC15"), 0.35);
                spawnPopup(player.x, arenaY - 18, "⚡ SURCHARGE PLASMA : MUNITIONS INFINIES (5.5s) !", Color.web("#FACC15"), 16, PopupStyle.HEX_TECH);
            }
        }
    }

    public void activateRedButtonNova() {
        if (currentScreen != ScreenState.GAMEPLAY || roundEnding) return;
        if (novaCharge < 100.0 || novaWaveActive) {
            spawnPopup(scene.getWidth() / 2.0, arenaY - 18, "☢️ NOVA EN CHARGE (" + (int) novaCharge + "%)", Color.web("#FF2A6D"), 15, PopupStyle.HEX_TECH);
            return;
        }

        novaCharge = 0;
        novaWaveActive = true;
        novaWaveRadius = 10.0;
        audio.playOrbitalBeam();
        triggerScreenShake(26.0, Color.web("#FACC15"), 0.55);
        spawnPopup(scene.getWidth() / 2.0, arenaY - 22, "☢️ DÉCHARGE NOVA PLASMA 360° ACTIVÉE !", Color.web("#FACC15"), 19, PopupStyle.STARBURST);
    }

    private void updateNovaSweep(double dt) {
        if (!novaWaveActive) return;
        novaWaveRadius += 540.0 * dt;
        shakeIntensity = Math.max(shakeIntensity, 9.0);

        List<ViralCell> splitList = new ArrayList<>();
        Iterator<ViralCell> it = viruses.iterator();
        while (it.hasNext()) {
            ViralCell v = it.next();
            double dist = Math.hypot(v.x - player.x, v.y - player.y);
            if (dist <= novaWaveRadius) {
                it.remove();
                // La Nova vaporise directement les Stades 1 et 2, et divise les Stades 3 !
                handleVirusDestructionAndMitosis(v, splitList, v.stage <= 2);
            }
        }
        viruses.addAll(splitList);

        if (novaWaveRadius >= Math.hypot(arenaW, arenaH)) {
            novaWaveActive = false;
        }
    }

    public void activateStasisRepulsion() {
        if (currentScreen != ScreenState.GAMEPLAY || roundEnding) return;
        if (stasisCharges <= 0) {
            spawnPopup(scene.getWidth() / 2.0, arenaY - 18, "⚠️ AUCUNE CHARGE DE STASE [R] !", Color.web("#FF2A6D"), 15, PopupStyle.WARNING_SHIELD);
            return;
        }

        stasisCharges--;
        stasisFreezeTimer = 4.5;
        hull = Math.min(maxHull, hull + 25.0);
        audio.playRollback();
        triggerScreenShake(16.0, Color.web("#38BDF8"), 0.45);
        shockwaves.add(new Shockwave(player.x, player.y, 320, Color.web("#38BDF8")));

        // Repousse tous les virus vers les bords de l'artère
        for (ViralCell v : viruses) {
            double ang = Math.atan2(v.y - player.y, v.x - player.x);
            v.vx = Math.cos(ang) * 320.0;
            v.vy = Math.sin(ang) * 320.0;
        }

        spawnPopup(scene.getWidth() / 2.0, arenaY - 22, "❄️ STASE QUANTIQUE (4.5s) + RÉPARATION COQUE !", Color.web("#38BDF8"), 17, PopupStyle.HEX_TECH);
    }

    public void triggerPlayerDash() {
        if (player.dashCooldown > 0) return;
        player.dashCooldown = 1.1;
        player.invulnerableTimer = 0.45;
        audio.playPowerup();

        double dashAngle = (Math.hypot(player.vx, player.vy) > 40)
                ? Math.atan2(player.vy, player.vx)
                : player.angle;

        player.vx += Math.cos(dashAngle) * 620.0;
        player.vy += Math.sin(dashAngle) * 620.0;
        shockwaves.add(new Shockwave(player.x, player.y, 55, Color.web("#05D5FA")));
        spawnExplosion(player.x, player.y, Color.web("#05D5FA"), 16, 180);
    }

    // =========================================================================
    // VICTOIRE DE VAGUE, SCORE & POP-UPS HORS-ARÈNE
    // =========================================================================

    private void triggerWaveVictory() {
        if (roundEnding) return;
        roundEnding = true;
        victoryDelayTimer = 2.0;
        audio.playPowerup();
        triggerScreenShake(22.0, Color.web("#4ADE80"), 0.45);

        int bonusXp = 650 + wave * 220;
        int bonusCredits = 240 + wave * 60;
        score += bonusXp;
        credits += bonusCredits;

        spawnPopup(
                scene.getWidth() / 2.0,
                arenaY - 22,
                "💥 VAGUE " + wave + " PURGÉE : +" + bonusXp + " XP & +" + bonusCredits + " $ !",
                Color.web("#4ADE80"),
                19,
                PopupStyle.STARBURST
        );
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

    // Déporte les pop-ups dans les marges gauche/droite pour garder l'artère 100% lisible
    public void spawnPopup(double x, double y, String text, Color color, int fontSize, PopupStyle style) {
        double safeX = x;
        double safeY = y;

        if (x > arenaX - 50 && x < arenaX + arenaW + 50 && y > arenaY - 10 && y < arenaY + arenaH + 10) {
            popupSideToggle++;
            if (popupSideToggle % 2 == 0) {
                safeX = Math.max(135, arenaX - 140);
            } else {
                safeX = Math.min(scene.getWidth() - 135, arenaX + arenaW + 140);
            }
            safeY = Math.max(arenaY + 45, Math.min(arenaY + arenaH - 40, y + ((popupSideToggle % 5) - 2) * 28));
        }

        while (floatingTexts.size() > 8) {
            floatingTexts.remove(0);
        }
        floatingTexts.add(new FloatingText(safeX, safeY, text, color, fontSize, style));
    }

    public void spawnExplosion(double x, double y, Color color, int count, double maxSpeed) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 40 + random.nextDouble() * maxSpeed;
            particles.add(new Particle(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, 4 + random.nextDouble() * 6, color, random.nextBoolean()));
        }
    }

    private void triggerScreenShake(double intensity, Color flashColor, double flashAlpha) {
        this.shakeIntensity = Math.max(this.shakeIntensity, intensity);
        this.screenFlashColor = flashColor;
        this.screenFlashAlpha = Math.max(this.screenFlashAlpha, flashAlpha);
    }

    private void updateVisualEffects(double dt) {
        particles.removeIf(p -> {
            p.update(dt);
            return p.life <= 0;
        });
        shockwaves.removeIf(s -> {
            s.update(dt);
            return s.life <= 0;
        });
        lasers.removeIf(l -> {
            l.update(dt);
            return l.life <= 0;
        });
        floatingTexts.removeIf(ft -> {
            ft.update(dt);
            return ft.life <= 0;
        });
    }

    // =========================================================================
    // RENDU GRAPHIQUE 360° (ARTÈRE BIO / FIBRE OPTIQUE + FUTURISTE vs BD)
    // =========================================================================

    private void renderGame() {
        double w = scene.getWidth();
        double h = scene.getHeight();

        gc.save();

        if (shakeIntensity > 0) {
            gc.translate((random.nextDouble() - 0.5) * 2 * shakeIntensity, (random.nextDouble() - 0.5) * 2 * shakeIntensity);
        }

        // 1. Fond global
        if (comicMode) {
            gc.setFill(Color.web("#FDF6E2"));
            gc.fillRect(-40, -40, w + 80, h + 80);

            gc.setFill(Color.web("#EF4444", 0.13));
            for (double dx = 0; dx < w; dx += 24) {
                for (double dy = 0; dy < h; dy += 24) {
                    gc.fillOval(dx, dy, 6, 6);
                }
            }
        } else {
            gc.setFill(Color.web("#050811"));
            gc.fillRect(-40, -40, w + 80, h + 80);

            gc.setStroke(Color.web("#0F172A", 0.85));
            gc.setLineWidth(1.0);
            double gridOffset = (time * 24) % 40;
            for (double gx = gridOffset; gx < w; gx += 40) gc.strokeLine(gx, 0, gx, h);
            for (double gy = gridOffset; gy < h; gy += 40) gc.strokeLine(0, gy, w, gy);
        }

        // 2. Rendu du Canal Central : ARTÈRE SANGUINE ou CÂBLE DE FIBRE OPTIQUE
        renderArteryOrFiberArena();

        // 3. Particules de Flux (Globules rouges ou Paquets de données)
        for (StreamParticle sp : backgroundStream) {
            if (bioArteryMode) {
                gc.setFill(Color.web("#EF4444", comicMode ? 0.35 : 0.22));
                gc.fillOval(sp.x - sp.size, sp.y - sp.size * 0.65, sp.size * 2, sp.size * 1.3);
            } else {
                gc.setFill(Color.web("#05D5FA", comicMode ? 0.40 : 0.25));
                gc.fillRoundRect(sp.x - sp.size * 1.4, sp.y - 2.5, sp.size * 2.8, 5, 3, 3);
            }
        }

        // 4. Cône du Vortex d'Aspiration Magnétique (si actif)
        if (player.vacuumActive) {
            renderVacuumCone();
        }

        // 5. Mines Bio-Plasma
        for (BioMine m : mines) {
            double r = 10 + 3 * Math.sin(m.pulse);
            gc.setStroke(Color.web("#D946EF", 0.8));
            gc.setLineWidth(2.2);
            gc.strokeOval(m.x - r, m.y - r, r * 2, r * 2);
            gc.setFill(Color.web("#FACC15"));
            gc.fillOval(m.x - 4, m.y - 4, 8, 8);
        }

        // 6. Rendu des Cellules Virales (Macro, Méso, Micro) avec Noyaux de Mitose
        for (ViralCell v : viruses) {
            v.render(gc, time, comicMode);
        }

        // 7. Projectiles Plasma, Nano-Missiles & Drones
        for (PlasmaBolt b : bolts) b.render(gc, comicMode);
        for (HomingNanite hn : naniteSwarm) hn.render(gc);
        for (OrbitalDrone d : drones) d.render(gc, comicMode);

        // 8. Rendu de la Nano-Sonde (Joueur) + Réticule 360°
        player.render(gc, time, comicMode, hyperSurgeTimer > 0);
        renderCrosshair();

        // 9. Onde Nova [ESPACE]
        if (novaWaveActive) {
            gc.setStroke(Color.web("#FACC15", 0.9));
            gc.setLineWidth(8.0);
            gc.strokeOval(player.x - novaWaveRadius, player.y - novaWaveRadius, novaWaveRadius * 2, novaWaveRadius * 2);
        }

        // 10. Effets Visuels (Ondes de choc, Lasers, Particules)
        for (Shockwave s : shockwaves) s.render(gc);
        for (CyberLaser l : lasers) l.render(gc);
        for (Particle p : particles) p.render(gc);

        // 11. HUD Tactique Complet + Pop-ups latéraux
        drawAdaptiveHUD();
        for (FloatingText ft : floatingTexts) ft.render(gc);

        if (screenFlashAlpha > 0) {
            gc.setFill(screenFlashColor.deriveColor(0, 1, 1, screenFlashAlpha));
            gc.fillRect(0, 0, w, h);
        }

        gc.restore();
    }

    private void renderArteryOrFiberArena() {
        Color borderCol = bioArteryMode ? Color.web("#FF2A6D") : Color.web("#05D5FA");
        Color innerBg = comicMode
                ? (bioArteryMode ? Color.web("#FFE4E6") : Color.web("#E0F2FE"))
                : (bioArteryMode ? Color.web("#1F0610") : Color.web("#061326"));

        if (comicMode) {
            gc.setFill(Color.BLACK);
            gc.fillRoundRect(arenaX + 5, arenaY + 5, arenaW, arenaH, 24, 24);
        }

        gc.setFill(innerBg);
        gc.fillRoundRect(arenaX, arenaY, arenaW, arenaH, 24, 24);

        // Lignes de flux organiques ou optiques à l'intérieur du tube
        gc.setStroke(borderCol.deriveColor(0, 1, 1, comicMode ? 0.22 : 0.16));
        gc.setLineWidth(2.0);
        for (int i = 1; i <= 4; i++) {
            double ly = arenaY + (arenaH * i) / 5.0 + Math.sin(time * 3.0 + i) * 6.0;
            gc.strokeLine(arenaX + 12, ly, arenaX + arenaW - 12, ly);
        }

        if (stasisFreezeTimer > 0) {
            gc.setFill(Color.web("#38BDF8", 0.16));
            gc.fillRoundRect(arenaX, arenaY, arenaW, arenaH, 24, 24);
        }

        // Paroi externe pulsatile
        double wallPulse = bioArteryMode ? (3.5 + 2.0 * Math.abs(Math.sin(time * 5.5))) : 3.5;
        gc.setStroke(comicMode ? Color.BLACK : borderCol);
        gc.setLineWidth(comicMode ? 4.5 : wallPulse);
        gc.strokeRoundRect(arenaX, arenaY, arenaW, arenaH, 24, 24);
    }

    private void renderVacuumCone() {
        double maxRange = 195.0 + (upgVacuumLevel - 1) * 42.0 + (selectedDeck == 0 ? 45.0 : 0);
        double halfDeg = 44.0 + (upgVacuumLevel - 1) * 7.0;
        double startDeg = -Math.toDegrees(player.angle) - halfDeg;

        gc.setFill(Color.web("#00FF9D", 0.16 + 0.06 * Math.sin(time * 18)));
        gc.fillArc(player.x - maxRange, player.y - maxRange, maxRange * 2, maxRange * 2, startDeg, halfDeg * 2, ArcType.ROUND);

        gc.setStroke(Color.web("#00FF9D", 0.65));
        gc.setLineWidth(2.0);
        gc.strokeArc(player.x - maxRange, player.y - maxRange, maxRange * 2, maxRange * 2, startDeg, halfDeg * 2, ArcType.ROUND);
    }

    private void renderCrosshair() {
        if (mouseX < arenaX || mouseX > arenaX + arenaW || mouseY < arenaY || mouseY > arenaY + arenaH) return;

        // Ligne laser de visée entre la Nano-Sonde et le curseur
        Color aimCol = player.vacuumActive ? Color.web("#00FF9D", 0.45) : Color.web("#05D5FA", 0.35);
        gc.setStroke(aimCol);
        gc.setLineWidth(1.4);
        gc.strokeLine(player.x, player.y, mouseX, mouseY);

        double r = 11.0;
        gc.setStroke(player.vacuumActive ? Color.web("#00FF9D") : Color.web("#FACC15"));
        gc.setLineWidth(2.0);
        gc.strokeOval(mouseX - r, mouseY - r, r * 2, r * 2);
        gc.strokeLine(mouseX - r - 4, mouseY, mouseX + r + 4, mouseY);
        gc.strokeLine(mouseX, mouseY - r - 4, mouseX, mouseY + r + 4);
    }

    // =========================================================================
    // HUD ADAPTATIF (STYLE HELLOAPPLICATION : HAUT, GAUCHE, DROITE, HOTBAR)
    // =========================================================================

    private void drawStyledBox(double x, double y, double w, double h, Color fill, Color borderColor) {
        if (comicMode) {
            gc.setFill(Color.BLACK);
            gc.fillRoundRect(x + 4, y + 4, w, h, 8, 8);
            gc.setFill(fill);
            gc.fillRoundRect(x, y, w, h, 8, 8);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3.0);
            gc.strokeRoundRect(x, y, w, h, 8, 8);
        } else {
            gc.setFill(Color.web("#0F172A", 0.90));
            gc.fillRoundRect(x, y, w, h, 10, 10);
            gc.setStroke(borderColor);
            gc.setLineWidth(1.6);
            gc.strokeRoundRect(x, y, w, h, 10, 10);
        }
    }

    private void drawHudText(String text, double x, double y, Color color, int size) {
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, size));
        gc.setFill(color);
        gc.fillText(text, x, y);
    }

    private void drawAdaptiveHUD() {
        double w = scene.getWidth();
        double topY = Math.max(22, arenaY - 92);
        Color panelBg = comicMode ? Color.web("#FACC15") : Color.web("#0F172A");
        Color textPrimary = comicMode ? Color.BLACK : Color.web("#E2E8F0");

        // 1. Haut-Gauche : Coque & Énergie Plasma
        double leftX = Math.max(16, arenaX - 274);
        Color accentCol = bioArteryMode ? Color.web("#FF2A6D") : Color.web("#05D5FA");
        drawStyledBox(leftX - 8, topY - 18, 256, 68, panelBg, accentCol);

        drawHudText("COQUE SONDE : " + (int) hull + "/" + (int) maxHull + " | PLASMA : " + (int) plasmaEnergy, leftX, topY, comicMode ? Color.BLACK : accentCol, 12);

        // Barre de Coque
        gc.setFill(Color.web("#1E293B"));
        gc.fillRoundRect(leftX, topY + 6, 114, 13, 5, 5);
        gc.setFill(Color.web("#4ADE80"));
        gc.fillRoundRect(leftX + 2, topY + 8, 110 * Math.min(1.0, hull / maxHull), 9, 4, 4);

        // Barre de Plasma
        gc.setFill(Color.web("#1E293B"));
        gc.fillRoundRect(leftX + 122, topY + 6, 114, 13, 5, 5);
        gc.setFill(hyperSurgeTimer > 0 ? Color.web("#FACC15") : Color.web("#05D5FA"));
        gc.fillRoundRect(leftX + 124, topY + 8, 110 * Math.min(1.0, plasmaEnergy / maxPlasmaEnergy), 9, 4, 4);

        drawHudText("🧲 ASPIRÉS : " + microAspiratedCount + " | ❄️ STASE [R] : " + stasisCharges, leftX, topY + 38, textPrimary, 11);

        // 2. Haut-Centre : Statut de la Mission & Mitose
        double centerBoxW = 450;
        double centerBoxX = (w - centerBoxW) / 2.0;
        drawStyledBox(centerBoxX, topY - 18, centerBoxW, 68, comicMode ? Color.web("#E62429") : Color.web("#0F172A"), accentCol);

        if (medalBannerTimer > 0) {
            drawHudText(activeMedalBanner, centerBoxX + 12, topY + 18, Color.web("#FACC15"), 12);
        } else {
            String envName = bioArteryMode ? "🩸 ARTÈRE SANGUINE" : "🌐 FIBRE OPTIQUE";
            drawHudText(envName + " — VAGUE " + wave + (comicMode ? " [STYLE BD]" : " [FUTURISTE]"), centerBoxX + 14, topY + 4, Color.web("#FACC15"), 13);
            drawHudText("Cellules actives : " + viruses.size() + " | Clic Gauche = Plasma | Clic Droit = Aspirer Micro-Virus !", centerBoxX + 14, topY + 28, Color.WHITE, 11);
        }

        // 3. Haut-Droite : Score, Crédits & Rang
        double rightX = arenaX + arenaW + 22;
        drawStyledBox(rightX - 8, topY - 18, 256, 68, panelBg, Color.web("#4ADE80"));
        drawHudText("SCORE : " + score + " XP | " + credits + "$", rightX, topY, comicMode ? Color.BLACK : Color.web("#4ADE80"), 14);
        drawHudText("🎖️ " + RANK_NAMES[currentRankIndex], rightX, topY + 20, comicMode ? Color.web("#B91C1C") : Color.web("#FACC15"), 13);
        drawHudText("🧬 MITOSES : " + totalSplits + " | COMBO x" + Math.max(1, combo), rightX, topY + 38, textPrimary, 12);

        drawLeftGuidePanel(leftX - 8, arenaY);
        drawRightTacticalPanel(rightX - 8, arenaY);
        drawBottomWeaponHotbar();
    }

    private void drawLeftGuidePanel(double px, double py) {
        Color boxBg = comicMode ? Color.WHITE : Color.web("#0F172A");
        Color txtColor = comicMode ? Color.BLACK : Color.WHITE;
        drawStyledBox(px, py, 256, 390, boxBg, bioArteryMode ? Color.web("#FF2A6D") : Color.web("#05D5FA"));

        drawHudText("// PILOTE NANO-SONDE 360° //", px + 12, py + 24, comicMode ? Color.web("#E62429") : Color.web("#05D5FA"), 13);

        String[][] guide = {
                {"WASD / FLÈCHES", "Propulsion Vectorielle Inertie", "#05D5FA"},
                {"CLIC GAUCHE", "Canon Plasma 360° (Mitose)", "#FACC15"},
                {"CLIC DROIT / [E]", "Vortex : Aspire Micro-Virus !", "#00FF9D"},
                {"MITOSE (1➔2➔4)", "Gros ➔ 2 Moyens ➔ 4 Micros", "#FF2A6D"},
                {"[SHIFT] DASH", "Impulsion d'Esquive Rapide", "#A855F7"},
                {"[R] STASE / [T] BD", "Repousse Virus / Mode Comic", "#38BDF8"}
        };

        for (int i = 0; i < guide.length; i++) {
            double y = py + 44 + i * 54;
            gc.setFill(Color.web(guide[i][2]));
            gc.fillRoundRect(px + 12, y, 16, 16, 4, 4);

            drawHudText(guide[i][0], px + 36, y + 13, txtColor, 13);
            gc.setFill(comicMode ? Color.web("#334155") : Color.web("#94A3B8"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
            gc.fillText(guide[i][1], px + 36, y + 29);
        }
    }

    private void drawRightTacticalPanel(double rx, double ry) {
        Color boxBg = comicMode ? Color.WHITE : Color.web("#0F172A");
        Color txtColor = comicMode ? Color.BLACK : Color.WHITE;
        drawStyledBox(rx, ry, 256, 260, boxBg, Color.web("#FACC15"));

        drawHudText("🧬 ANALYSE DE LA MITOSE :", rx + 14, ry + 24, comicMode ? Color.web("#E62429") : Color.web("#FF2A6D"), 13);

        int macroCount = 0, mesoCount = 0, microCount = 0;
        for (ViralCell v : viruses) {
            if (v.stage == 3) macroCount++;
            else if (v.stage == 2) mesoCount++;
            else microCount++;
        }

        gc.setFill(txtColor);
        gc.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        gc.fillText("• Gros Virus (Stade 3)   : " + (macroCount + macroRemainingToSpawn), rx + 14, ry + 48);
        gc.fillText("• Virus Moyens (Stade 2) : " + mesoCount, rx + 14, ry + 66);
        gc.fillText("• Micro-Virus (Stade 1)  : " + microCount, rx + 14, ry + 84);
        gc.fillText("• Drones Orbitaux Actifs : " + drones.size(), rx + 14, ry + 102);

        // Barre de Surcharge Plasma
        double sy = ry + 130;
        drawHudText("SURCHARGE PLASMA INFINIE", rx + 14, sy, txtColor, 12);
        gc.setFill(Color.web("#1E293B"));
        gc.fillRoundRect(rx + 14, sy + 8, 228, 15, 6, 6);
        double ratio = (hyperSurgeTimer > 0) ? (hyperSurgeTimer / 5.5) : (hyperSurgeCharge / HYPER_SURGE_COOLDOWN);
        gc.setFill(hyperSurgeTimer > 0 ? Color.web("#FACC15") : Color.web("#05D5FA"));
        gc.fillRoundRect(rx + 16, sy + 10, 224 * Math.min(1.0, ratio), 11, 5, 5);

        // BOUTON ROUGE : DÉCHARGE NOVA [ESPACE]
        double redY = ry + 274;
        boolean ready = novaCharge >= 100.0;
        drawStyledBox(rx, redY, 256, 56, ready ? Color.web("#FACC15") : Color.web("#991B1B"), Color.web("#FF2A6D"));
        drawHudText("☢️ NOVA PLASMA 360° [ESPACE]", rx + 14, redY + 22, (ready && comicMode) ? Color.BLACK : Color.WHITE, 13);
        drawHudText(ready ? ">> PRÊT ! ESPACE OU CLIC <<" : "CHARGE : " + (int) novaCharge + "%", rx + 14, redY + 42, (ready && comicMode) ? Color.BLACK : Color.web("#FEF08A"), 11);

        // BOUTON STASE QUANTIQUE [R]
        double rbY = ry + 340;
        drawStyledBox(rx, rbY, 256, 52, stasisCharges > 0 ? Color.web("#0284C7") : Color.web("#1E293B"), Color.web("#38BDF8"));
        drawHudText("❄️ STASE & RÉPULSION [R]", rx + 14, rbY + 21, Color.WHITE, 13);
        drawHudText("Gèle la Mitose 4.5s (Charges : " + stasisCharges + ")", rx + 14, rbY + 40, Color.web("#BAE6FD"), 11);
    }

    private void drawBottomWeaponHotbar() {
        String[][] weapons = {
                {"[1] CANON PLASMA", "Tir 360° Rapide + Ricochet", "#05D5FA"},
                {"[2] DISPERSION 5X", "Éventail Plasma Large", "#FACC15"},
                {"[3] ONDE DE CHOC", "Repousse & Brise Mitose", "#38BDF8"},
                {"[4] VORTEX MAGNÉTIQUE", "Aspire les Micro-Virus !", "#00FF9D"},
                {"[5] MINE BIO-PLASMA", "Piège Explosif de Zone", "#D946EF"},
                {"[6] ESSAIM NANITES", "4 Missiles Autoguidés", "#F97316"}
        };

        double cardW = 166;
        double cardH = 56;
        double gap = 10;
        double totalW = 6 * cardW + 5 * gap;
        double startX = (scene.getWidth() - totalW) / 2.0;
        double y = arenaY + arenaH + 16;

        for (int i = 0; i < 6; i++) {
            double x = startX + i * (cardW + gap);
            boolean active = (selectedWeapon == i + 1) || (i == 3 && player.vacuumActive);
            Color theme = Color.web(weapons[i][2]);
            Color fill = active ? theme : (comicMode ? Color.WHITE : Color.web("#0F172A"));

            drawStyledBox(x, y, cardW, cardH, fill, active ? theme : Color.web("#334155"));
            drawHudText(weapons[i][0], x + 8, y + 21, comicMode || active ? Color.BLACK : theme, 12);

            gc.setFill(comicMode || active ? Color.web("#0F172A") : Color.web("#CBD5E1"));
            gc.setFont(Font.font("Consolas", FontWeight.BOLD, 10));
            gc.fillText(weapons[i][1], x + 8, y + 40);
        }
    }

    // =========================================================================
    // MENUS : ACCUEIL, BOUTIQUE D'AMÉLIORATIONS & GAME OVER
    // =========================================================================

    public void loadNewScreen(ScreenState state) {
        updateLayoutMetrics();
        pane.getChildren().clear();
        pane.getChildren().add(bgCanvas);
        currentScreen = state;
        activeKeys.clear();
        isShootingHold = false;
        isVacuumHold = false;

        if (state == ScreenState.GAMEPLAY) {
            comicMode = (wave % 2 == 0);
        }

        switch (state) {
            case ACCUEIL -> {
                audio.setMusicActive(false);
                buildAccueilOverlay();
            }
            case GAMEPLAY -> {
                audio.setMusicActive(true);
                initWave();
            }
            case SHOP -> {
                audio.playPowerup();
                renderGame();
                buildShopOverlay();
            }
            case GAME_OVER -> {
                audio.setMusicActive(false);
                renderGame();
                buildGameOverOverlay();
            }
        }
    }

    private void initWave() {
        updateLayoutMetrics();
        player.x = arenaX + arenaW / 2.0;
        player.y = arenaY + arenaH / 2.0;
        player.vx = 0;
        player.vy = 0;
        hull = maxHull;
        plasmaEnergy = maxPlasmaEnergy;

        viruses.clear();
        bolts.clear();
        mines.clear();
        naniteSwarm.clear();
        drones.clear();
        particles.clear();
        shockwaves.clear();
        lasers.clear();
        floatingTexts.clear();

        int totalDrones = droneCount + (selectedDeck == 2 ? 1 : 0);
        for (int i = 0; i < totalDrones; i++) {
            drones.add(new OrbitalDrone(i * (Math.PI * 2.0 / Math.max(1, totalDrones))));
        }

        // Vague initiale : fait apparaître immédiatement 2 Macro-Virus puis le reste progressivement
        int totalMacroThisWave = 2 + wave;
        for (int i = 0; i < 2; i++) {
            spawnMacroVirusOnEdge();
        }
        macroRemainingToSpawn = totalMacroThisWave - 2;
        spawnTimer = 2.8;
        roundEnding = false;
    }

    private void applySelectedDeckStats() {
        maxHull = (selectedDeck == 0) ? 145.0 : 100.0;
        hull = maxHull;
        maxPlasmaEnergy = (selectedDeck == 1) ? 185.0 : 150.0;
        plasmaEnergy = maxPlasmaEnergy;
        plasmaRegen = (selectedDeck == 2) ? 54.0 : 42.0;

        upgWeaponLevel = 1;
        upgVacuumLevel = 1;
        upgThrusterLevel = 1;
        droneCount = 0;
        stasisCharges = 2;
        novaCharge = 50.0;
        credits = 250;
        wave = 1;
        score = 0;
        totalSplits = 0;
        microAspiratedCount = 0;
        microDisintegratedCount = 0;
        totalMedalsEarned = 0;
        currentRankIndex = 0;
    }

    private void buildAccueilOverlay() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        root.setStyle("-fx-background-color: rgba(5, 8, 17, 0.95);");

        Label title = new Label("// NANO-SONDE : FLUX SANGUIN & FIBRE OPTIQUE //");
        title.setTextFill(Color.web("#05D5FA"));
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 32));

        Label sub = new Label("SHOOT'EM UP 360° VECTORIEL & MITOSE CELLULAIRE (1 GROS ➔ 2 MOYENS ➔ 4 MICRO-VIRUS)");
        sub.setTextFill(Color.web("#FACC15"));
        sub.setFont(Font.font("Consolas", FontWeight.BOLD, 15));

        HBox envBox = new HBox(20);
        envBox.setAlignment(Pos.CENTER);
        Button btnBio = new Button("🩸 MODE FLUX SANGUIN (ARTÈRE BIO)\nParois artérielles pulsatiles, globules rouges,\ncourant systolique et souches organiques !");
        Button btnFiber = new Button("🌐 MODE FIBRE OPTIQUE (CÂBLE QUANTIQUE)\nCoeur photonique haute vitesse, paquets binaires,\ntirs plasma à ricochets sur les parois !");

        HBox deckBox = new HBox(18);
        deckBox.setAlignment(Pos.CENTER);
        Button btnDeck0 = new Button("🛡️ SONDE TITAN-BIO\nCoque Renforcée (145 PV)\nVortex d'Aspiration Géant\nBouclier de Choc");
        Button btnDeck1 = new Button("⚡ SONDE HYPER-PLASMA\nCadence de Tir +35%\nProjectiles à Ricochets\nRéserve Plasma 185");
        Button btnDeck2 = new Button("🤖 SONDE SWARM-TECH\n1 Nano-Drone dès Niveau 1\nRégénération Plasma +30%\nEssaim Guidé Boosté");
        Button[] deckBtns = {btnDeck0, btnDeck1, btnDeck2};
        String[] deckCols = {"#00FF9D", "#05D5FA", "#FACC15"};

        Button btnLaunch = new Button();
        btnLaunch.setFont(Font.font("Consolas", FontWeight.BOLD, 20));

        Runnable refreshUI = () -> {
            btnBio.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
            btnBio.setPrefSize(430, 86);
            btnBio.setStyle("-fx-background-color: " + (bioArteryMode ? "#4C0519" : "#0F172A") + "; -fx-text-fill: " + (bioArteryMode ? "#FF2A6D" : "#64748B") + "; -fx-border-color: " + (bioArteryMode ? "#FF2A6D" : "#334155") + "; -fx-border-width: 3; -fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");

            btnFiber.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
            btnFiber.setPrefSize(430, 86);
            btnFiber.setStyle("-fx-background-color: " + (!bioArteryMode ? "#0C4A6E" : "#0F172A") + "; -fx-text-fill: " + (!bioArteryMode ? "#38BDF8" : "#64748B") + "; -fx-border-color: " + (!bioArteryMode ? "#05D5FA" : "#334155") + "; -fx-border-width: 3; -fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");

            for (int i = 0; i < 3; i++) {
                boolean act = (selectedDeck == i);
                deckBtns[i].setFont(Font.font("Consolas", FontWeight.BOLD, 13));
                deckBtns[i].setPrefSize(290, 105);
                deckBtns[i].setStyle("-fx-background-color: " + (act ? "#1E293B" : "#0F172A") + "; -fx-text-fill: " + (act ? deckCols[i] : "#94A3B8") + "; -fx-border-color: " + (act ? deckCols[i] : "#334155") + "; -fx-border-width: " + (act ? "3" : "1.5") + "; -fx-background-radius: 10; -fx-border-radius: 10; -fx-cursor: hand;");
            }

            btnLaunch.setText(bioArteryMode ? ">> INJECTER LA NANO-SONDE DANS L'ARTÈRE (VAGUE 1) <<" : ">> INJECTER LA NANO-SONDE DANS LA FIBRE (VAGUE 1) <<");
            btnLaunch.setStyle("-fx-background-color: " + (bioArteryMode ? "#FF2A6D" : "#05D5FA") + "; -fx-text-fill: " + (bioArteryMode ? "white" : "#050811") + "; -fx-padding: 14 42 14 42; -fx-background-radius: 8; -fx-cursor: hand;");
        };

        btnBio.setOnAction(e -> {
            bioArteryMode = true;
            audio.playPowerup();
            refreshUI.run();
        });
        btnFiber.setOnAction(e -> {
            bioArteryMode = false;
            audio.playPowerup();
            refreshUI.run();
        });

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            deckBtns[i].setOnAction(e -> {
                selectedDeck = idx;
                audio.playPowerup();
                refreshUI.run();
            });
        }

        refreshUI.run();
        envBox.getChildren().addAll(btnBio, btnFiber);
        deckBox.getChildren().addAll(btnDeck0, btnDeck1, btnDeck2);

        btnLaunch.setOnAction(e -> {
            applySelectedDeckStats();
            loadNewScreen(ScreenState.GAMEPLAY);
        });

        root.getChildren().addAll(title, sub, envBox, deckBox, btnLaunch);

        if (onBackToMainCallback != null) {
            Button btnBack = new Button("<< RETOUR À CYBER-CONTAINMENT (GRILLE SOC) >>");
            btnBack.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
            btnBack.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #E2E8F0; -fx-padding: 8 24 8 24; -fx-background-radius: 6; -fx-cursor: hand;");
            btnBack.setOnAction(e -> {
                audio.stopEngine();
                onBackToMainCallback.run();
            });
            root.getChildren().add(btnBack);
        }

        pane.getChildren().add(root);
    }

    private void buildShopOverlay() {
        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        root.setStyle("-fx-background-color: rgba(5, 8, 17, 0.92);");

        Label title = new Label("// LABORATOIRE NANO-TECH [VAGUE " + (wave - 1) + " PURGÉE] //");
        title.setTextFill(Color.web("#05D5FA"));
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 28));

        Label stats = new Label();
        stats.setTextFill(Color.web("#FACC15"));
        stats.setFont(Font.font("Consolas", FontWeight.BOLD, 17));

        Runnable updateStats = () -> stats.setText("💳 CRÉDITS : " + credits + " $   |   ❄️ STASES : " + stasisCharges + "   |   🤖 DRONES : " + droneCount + "   |   🎖️ " + RANK_NAMES[currentRankIndex]);
        updateStats.run();

        VBox rows = new VBox(8);
        rows.setAlignment(Pos.CENTER);

        rows.getChildren().add(createShopRow(
                "🔫 CANON PLASMA MULTI-FAISCEAUX (+1 Faisceau / Dégâts)",
                () -> "Niv." + upgWeaponLevel,
                () -> 180 + (upgWeaponLevel - 1) * 140,
                () -> {
                    upgWeaponLevel++;
                    updateStats.run();
                }
        ));

        rows.getChildren().add(createShopRow(
                "🧲 PUISSANCE DU VORTEX ASPIRATEUR (+Portée & Vitesse d'aspiration)",
                () -> "Niv." + upgVacuumLevel,
                () -> 150 + (upgVacuumLevel - 1) * 110,
                () -> {
                    upgVacuumLevel++;
                    updateStats.run();
                }
        ));

        rows.getChildren().add(createShopRow(
                "🚀 PROPULSEURS VECTORIELS & COQUE MAX (+25 PV & Vitesse)",
                () -> "Niv." + upgThrusterLevel + " (" + (int) maxHull + " PV)",
                () -> 150 + (upgThrusterLevel - 1) * 100,
                () -> {
                    upgThrusterLevel++;
                    maxHull += 25.0;
                    hull = maxHull;
                    updateStats.run();
                }
        ));

        rows.getChildren().add(createShopRow(
                "🤖 ASSEMBLER UN NANO-DRONE ORBITAL (+1 Drone Laser)",
                () -> "Escouade : " + droneCount,
                () -> 210 + droneCount * 95,
                () -> {
                    droneCount++;
                    updateStats.run();
                }
        ));

        rows.getChildren().add(createShopRow(
                "☢️ RECHARGE NOVA 100% + 2 STASES QUANTIQUES [R]",
                () -> "Nova : " + (int) novaCharge + "%",
                () -> 170,
                () -> {
                    novaCharge = 100.0;
                    stasisCharges += 2;
                    updateStats.run();
                }
        ));

        String nextStyle = (wave % 2 == 0) ? "STYLE BANDE DESSINÉE" : "STYLE FUTURISTE";
        Button btnNext = new Button(">> PLONGER DANS LA VAGUE " + wave + " (" + nextStyle + ") <<");
        btnNext.setFont(Font.font("Consolas", FontWeight.BOLD, 19));
        btnNext.setStyle("-fx-background-color: #4ADE80; -fx-text-fill: #050811; -fx-padding: 12 36 12 36; -fx-background-radius: 8; -fx-cursor: hand;");
        btnNext.setOnAction(e -> loadNewScreen(ScreenState.GAMEPLAY));

        root.getChildren().addAll(title, stats, rows, btnNext);
        pane.getChildren().add(root);
    }

    private HBox createShopRow(String name, java.util.function.Supplier<String> statusSup, java.util.function.Supplier<Integer> costSup, Runnable onBuy) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefWidth(820);
        row.setMaxWidth(820);
        row.setStyle("-fx-background-color: #0F172A; -fx-border-color: #05D5FA; -fx-border-width: 1.5; -fx-padding: 10 18 10 18; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label lblName = new Label(name);
        lblName.setTextFill(Color.WHITE);
        lblName.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        lblName.setPrefWidth(460);

        Label lblStatus = new Label(statusSup.get());
        lblStatus.setTextFill(Color.web("#38BDF8"));
        lblStatus.setFont(Font.font("Consolas", FontWeight.BOLD, 14));
        lblStatus.setPrefWidth(150);

        Button btnBuy = new Button("ACHETER (" + costSup.get() + " $)");
        btnBuy.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        btnBuy.setStyle("-fx-background-color: #FACC15; -fx-text-fill: black; -fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand;");

        btnBuy.setOnAction(e -> {
            int cost = costSup.get();
            if (credits >= cost) {
                credits -= cost;
                audio.playPowerup();
                onBuy.run();
                lblStatus.setText(statusSup.get());
                btnBuy.setText("ACHETER (" + costSup.get() + " $)");
            } else {
                btnBuy.setStyle("-fx-background-color: #FF2A6D; -fx-text-fill: white; -fx-padding: 7 16 7 16; -fx-background-radius: 6;");
            }
        });

        row.getChildren().addAll(lblName, lblStatus, btnBuy);
        return row;
    }

    private void buildGameOverOverlay() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        root.setStyle("-fx-background-color: rgba(5, 8, 17, 0.92);");

        Label title = new Label("// NANO-SONDE SUBMERGÉE PAR LA MITOSE VIRALE //");
        title.setTextFill(Color.web("#FF2A6D"));
        title.setFont(Font.font("Consolas", FontWeight.BOLD, 32));

        Label sub = new Label("SCORE : " + score + " XP  |  MICRO-VIRUS ASPIRÉS : " + microAspiratedCount + "  |  RANG : " + RANK_NAMES[currentRankIndex]);
        sub.setTextFill(Color.web("#E2E8F0"));
        sub.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        Button btnRestart = new Button(">> RECONFIGURER UNE NOUVELLE NANO-SONDE <<");
        btnRestart.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        btnRestart.setStyle("-fx-background-color: #05D5FA; -fx-text-fill: #050811; -fx-padding: 14 38 14 38; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRestart.setOnAction(e -> loadNewScreen(ScreenState.ACCUEIL));

        root.getChildren().addAll(title, sub, btnRestart);
        pane.getChildren().add(root);
    }

    // =========================================================================
    // CONTRÔLES CLAVIER (WASD / ZQSD) & SOURIS 360°
    // =========================================================================

    private void setupControls(Stage stage) {
        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            activeKeys.add(code);

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
                return;
            }
            if (currentScreen != ScreenState.GAMEPLAY) return;

            if (code == KeyCode.SPACE) activateRedButtonNova();
            else if (code == KeyCode.R) activateStasisRepulsion();
            else if (code == KeyCode.SHIFT) triggerPlayerDash();
            else if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) selectedWeapon = 1;
            else if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) selectedWeapon = 2;
            else if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) selectedWeapon = 3;
            else if (code == KeyCode.DIGIT4 || code == KeyCode.NUMPAD4) selectedWeapon = 4;
            else if (code == KeyCode.DIGIT5 || code == KeyCode.NUMPAD5) selectedWeapon = 5;
            else if (code == KeyCode.DIGIT6 || code == KeyCode.NUMPAD6) selectedWeapon = 6;
        });

        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        bgCanvas.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        bgCanvas.setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        bgCanvas.setOnMousePressed(e -> {
            mouseX = e.getX();
            mouseY = e.getY();

            if (currentScreen != ScreenState.GAMEPLAY) return;

            // Clic sur les boutons tactiques à droite (Nova / Stase)
            double rightPanelX = arenaX + arenaW + 14;
            if (mouseX >= rightPanelX && mouseX <= rightPanelX + 256) {
                if (mouseY >= arenaY + 274 && mouseY <= arenaY + 330) {
                    activateRedButtonNova();
                    return;
                }
                if (mouseY >= arenaY + 340 && mouseY <= arenaY + 392) {
                    activateStasisRepulsion();
                    return;
                }
            }

            // Clic sur la Hotbar en bas
            double hotbarY = arenaY + arenaH + 16;
            if (mouseY >= hotbarY && mouseY <= hotbarY + 58) {
                double cardW = 166, gap = 10;
                double startX = (scene.getWidth() - (6 * cardW + 5 * gap)) / 2.0;
                for (int i = 0; i < 6; i++) {
                    double x0 = startX + i * (cardW + gap);
                    if (mouseX >= x0 && mouseX <= x0 + cardW) {
                        selectedWeapon = i + 1;
                        return;
                    }
                }
            }

            if (e.getButton() == MouseButton.PRIMARY) {
                isShootingHold = true;
            } else if (e.getButton() == MouseButton.SECONDARY) {
                isVacuumHold = true;
            }
        });

        bgCanvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                isShootingHold = false;
            } else if (e.getButton() == MouseButton.SECONDARY) {
                isVacuumHold = false;
            }
        });
    }

    // =========================================================================
    // CLASSES D'ENTITÉS (NANO-SONDE, CELLULE VIRALE À MITOSE, PLASMA, EFFETS)
    // =========================================================================

    public static class NanoProbe {
        public double x, y, vx = 0, vy = 0;
        public double angle = 0;
        public double radius = 15.0;
        public double fireCooldown = 0;
        public double dashCooldown = 0;
        public double invulnerableTimer = 0;
        public boolean vacuumActive = false;

        public NanoProbe(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void render(GraphicsContext gc, double time, boolean comicMode, boolean surge) {
            gc.save();
            gc.translate(x, y);
            gc.rotate(Math.toDegrees(angle));

            // Anneau de bouclier rotatif
            Color ringCol = vacuumActive ? Color.web("#00FF9D") : (surge ? Color.web("#FACC15") : Color.web("#05D5FA"));
            gc.setStroke(ringCol);
            gc.setLineWidth(2.2);
            gc.strokeOval(-radius - 4, -radius - 4, (radius + 4) * 2, (radius + 4) * 2);

            // Vaisseau / Nano-Sonde triangulaire profilée
            double[] px = {radius + 6, -radius, -radius * 0.55, -radius};
            double[] py = {0, -radius * 0.85, 0, radius * 0.85};

            if (comicMode) {
                gc.setFill(Color.BLACK);
                gc.fillPolygon(new double[]{px[0] + 2, px[1] + 2, px[2] + 2, px[3] + 2}, new double[]{py[0] + 2, py[1] + 2, py[2] + 2, py[3] + 2}, 4);
            }

            gc.setFill(invulnerableTimer > 0 && ((int) (time * 20) % 2 == 0) ? Color.WHITE : ringCol);
            gc.fillPolygon(px, py, 4);

            gc.setStroke(comicMode ? Color.BLACK : Color.WHITE);
            gc.setLineWidth(2.0);
            gc.strokePolygon(px, py, 4);

            // Cœur énergétique
            gc.setFill(Color.WHITE);
            gc.fillOval(-4, -4, 8, 8);

            gc.restore();
        }
    }

    public static class ViralCell {
        public double x, y, vx, vy;
        public int stage; // 3 = Macro (Gros), 2 = Méso (Moyen), 1 = Micro (Petit ultra-rapide)
        public double radius;
        public double maxHp, hp;
        public StrainType strain;
        public double wobblePhase;
        public double hitFlash = 0;

        public ViralCell(double x, double y, int stage, StrainType strain, int wave) {
            this.x = x;
            this.y = y;
            this.stage = stage;
            this.strain = strain;
            this.wobblePhase = Math.random() * Math.PI * 2;

            if (stage == 3) {
                this.radius = 34.0;
                this.maxHp = (5.0 + wave * 1.1) * strain.hpMult;
            } else if (stage == 2) {
                this.radius = 20.0;
                this.maxHp = (2.6 + wave * 0.45) * strain.hpMult;
            } else {
                this.radius = 9.5;
                this.maxHp = 1.0; // Les Micro-Virus (Stade 1) se détruisent en 1 tir ou s'aspirent !
            }
            this.hp = this.maxHp;
        }

        public void render(GraphicsContext gc, double time, boolean comicMode) {
            if (hitFlash > 0) hitFlash = Math.max(0, hitFlash - 0.12);

            Color mainCol = hitFlash > 0 ? Color.WHITE : Color.web(strain.hexMain);
            Color coreCol = Color.web(strain.hexCore);

            // Membrane cellulaire déformable (pseudopodes avant mitose)
            int pts = (stage == 3) ? 18 : (stage == 2 ? 14 : 10);
            double[] px = new double[pts];
            double[] py = new double[pts];

            for (int i = 0; i < pts; i++) {
                double ang = (i * Math.PI * 2.0) / pts;
                double deform = Math.sin(ang * (stage + 2) + wobblePhase) * (radius * 0.14);
                double r = radius + deform;
                px[i] = x + Math.cos(ang) * r;
                py[i] = y + Math.sin(ang) * r;
            }

            if (comicMode) {
                gc.setFill(Color.BLACK);
                gc.fillOval(x - radius + 3, y - radius + 3, radius * 2, radius * 2);
            }

            gc.setFill(mainCol.deriveColor(0, 1, 1, 0.82));
            gc.fillPolygon(px, py, pts);

            gc.setStroke(comicMode ? Color.BLACK : Color.WHITE);
            gc.setLineWidth(comicMode ? 2.5 : 1.8);
            gc.strokePolygon(px, py, pts);

            // Affichage visuel des noyaux internes préfigurant la mitose !
            if (stage == 3) {
                // 2 noyaux visibles qui s'écartent quand la cellule est proche de la mitose
                double splitSep = 7.0 + (1.0 - hp / maxHp) * 7.0;
                gc.setFill(coreCol);
                gc.fillOval(x - splitSep - 6, y - 6, 12, 12);
                gc.fillOval(x + splitSep - 6, y - 6, 12, 12);
            } else if (stage == 2) {
                // 4 micro-noyaux visibles (préfigurant les 4 micro-virus)
                gc.setFill(coreCol);
                double d = 5.5;
                gc.fillOval(x - d - 3, y - d - 3, 6, 6);
                gc.fillOval(x + d - 3, y - d - 3, 6, 6);
                gc.fillOval(x - d - 3, y + d - 3, 6, 6);
                gc.fillOval(x + d - 3, y + d - 3, 6, 6);
            } else {
                // Stade 1 (Micro-Virus) : noyau brillant
                gc.setFill(Color.WHITE);
                gc.fillOval(x - 3.5, y - 3.5, 7, 7);
            }
        }
    }

    public static class PlasmaBolt {
        double x, y, vx, vy, damage;
        double radius = 5.0;
        double life = 1.6;
        Color color;
        int bouncesLeft;

        public PlasmaBolt(double x, double y, double vx, double vy, double damage, Color color, int bouncesLeft) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.damage = damage;
            this.color = color;
            this.bouncesLeft = bouncesLeft;
        }

        public void update(double dt) {
            x += vx * dt;
            y += vy * dt;
            life -= dt;
        }

        public void render(GraphicsContext gc, boolean comicMode) {
            if (comicMode) {
                gc.setFill(Color.BLACK);
                gc.fillOval(x - 6, y - 6, 12, 12);
            }
            gc.setFill(color);
            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.setFill(Color.WHITE);
            gc.fillOval(x - 2.5, y - 2.5, 5, 5);
        }
    }

    public static class BioMine {
        double x, y, pulse = 0;

        public BioMine(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class HomingNanite {
        double x, y, angle, speed = 420.0, life = 2.8;

        public HomingNanite(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        public void update(double dt, ViralCell target) {
            life -= dt;
            if (target != null) {
                double desired = Math.atan2(target.y - y, target.x - x);
                double diff = desired - angle;
                while (diff > Math.PI) diff -= Math.PI * 2;
                while (diff < -Math.PI) diff += Math.PI * 2;
                angle += diff * 8.0 * dt;
            }
            x += Math.cos(angle) * speed * dt;
            y += Math.sin(angle) * speed * dt;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(Color.web("#F97316"));
            gc.fillOval(x - 4.5, y - 4.5, 9, 9);
            gc.setFill(Color.WHITE);
            gc.fillOval(x - 2, y - 2, 4, 4);
        }
    }

    public static class OrbitalDrone {
        double x, y, angle, fireCooldown = 0.4;

        public OrbitalDrone(double startAngle) {
            this.angle = startAngle;
        }

        public void update(double dt, double centerX, double centerY, double orbitRadius) {
            angle += dt * 2.6;
            x = centerX + Math.cos(angle) * orbitRadius;
            y = centerY + Math.sin(angle) * orbitRadius;
            if (fireCooldown > 0) fireCooldown -= dt;
        }

        public void render(GraphicsContext gc, boolean comicMode) {
            if (comicMode) {
                gc.setFill(Color.BLACK);
                gc.fillOval(x - 8, y - 8, 16, 16);
            }
            gc.setStroke(Color.web("#10B981"));
            gc.setLineWidth(2.0);
            gc.strokeOval(x - 7, y - 7, 14, 14);
            gc.setFill(Color.web("#FACC15"));
            gc.fillOval(x - 3.5, y - 3.5, 7, 7);
        }
    }

    public static class StreamParticle {
        double x, y, speed, size, phase;

        public StreamParticle(double x, double y, double speed, double size) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
            this.phase = Math.random() * Math.PI * 2;
        }
    }

    public static class Particle {
        double x, y, vx, vy, size, life = 1.0;
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
            life -= dt * 1.6;
        }

        public void render(GraphicsContext gc) {
            double alpha = Math.max(0, life);
            double s = size * alpha;
            gc.setFill(color.deriveColor(0, 1, 1, alpha));
            if (isSquare) gc.fillRect(x - s / 2, y - s / 2, s, s);
            else gc.fillOval(x - s / 2, y - s / 2, s, s);
        }
    }

    public static class Shockwave {
        double x, y, maxRadius, life = 1.0;
        Color color;

        public Shockwave(double x, double y, double maxRadius, Color color) {
            this.x = x;
            this.y = y;
            this.maxRadius = maxRadius;
            this.color = color;
        }

        public void update(double dt) {
            life -= dt * 2.2;
        }

        public void render(GraphicsContext gc) {
            double progress = 1.0 - Math.max(0, life);
            double r = progress * maxRadius;
            gc.setStroke(color.deriveColor(0, 1, 1, Math.max(0, life)));
            gc.setLineWidth(3.2 * Math.max(0, life));
            gc.strokeOval(x - r, y - r, r * 2, r * 2);
        }
    }

    public static class CyberLaser {
        double x1, y1, x2, y2, width, life = 1.0;
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
            double a = Math.max(0, life);
            gc.setStroke(color.deriveColor(0, 1, 1, a * 0.75));
            gc.setLineWidth(width * 2.0 * a);
            gc.strokeLine(x1, y1, x2, y2);
            gc.setStroke(Color.color(1, 1, 1, a));
            gc.setLineWidth(width * 0.7 * a);
            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    public static class FloatingText {
        double x, y, vx, vy, life = 1.0, age = 0.0;
        String text;
        Color color;
        int baseFontSize;
        PopupStyle style;

        public FloatingText(double x, double y, String text, Color color, int baseFontSize, PopupStyle style) {
            this.x = x;
            this.y = y;
            this.vx = (Math.random() - 0.5) * 16.0;
            this.vy = -48.0;
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
            double scale = age < 0.10 ? (0.5 + (age / 0.10) * 0.75) : 1.0;
            double fontSize = baseFontSize * scale;

            gc.setFont(Font.font("Consolas", FontWeight.BOLD, fontSize));
            double textW = text.length() * fontSize * 0.56;
            double boxW = textW + 22;
            double boxH = fontSize + 10;
            double cx = x;
            double cy = y - fontSize * 0.35;

            gc.setFill(Color.color(0.04, 0.07, 0.15, alpha * 0.86));
            gc.fillRoundRect(cx - boxW / 2.0, cy - boxH / 2.0, boxW, boxH, 10, 10);
            gc.setStroke(color.deriveColor(0, 1, 1, alpha));
            gc.setLineWidth(2.0);
            gc.strokeRoundRect(cx - boxW / 2.0, cy - boxH / 2.0, boxW, boxH, 10, 10);

            gc.setFill(color.deriveColor(0, 1, 1, alpha));
            gc.fillText(text, cx - textW / 2.0, y);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
