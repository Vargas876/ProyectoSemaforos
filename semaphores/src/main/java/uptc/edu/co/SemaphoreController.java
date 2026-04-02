package uptc.edu.co;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;

public class SemaphoreController {
    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private VBox optionsVBox;

    @FXML
    private Button setupButton;

    @FXML
    private Button stopButton;

    @FXML 
    private Button resetButton;

    @FXML
    private TextField northCarsTextField;

    @FXML
    private TextField eastCarsTextField;

    @FXML
    private TextField southCarsTextField;

    @FXML
    private TextField westCarsTextField;

    @FXML
    private TextField permitsTextField;

    @FXML
    private ImageView semaphoreEast;

    @FXML
    private ImageView semaphoreNorth;

    @FXML
    private ImageView semaphoreSouth;

    @FXML
    private ImageView semaphoreWest;

    @FXML
    private Label northCarsCountLabel;
    private int northCarsCount = 0;

    @FXML
    private Label eastCarsCountLabel;
    private int eastCarsCount = 0;

    @FXML
    private Label southCarsCountLabel;
    private int southCarsCount = 0;

    @FXML
    private Label westCarsCountLabel;
    private int westCarsCount = 0;
    

    private Image car1Image;
    private Image car2Image;
    private Image car3Image;
    private Image car4Image;
    private Image car5Image;
    private Image car6Image;
    private Image car7Image;
    private Image car8Image;
    private Image car9Image;
    private Image car10Image;
    private List<ImageView> carImageViews;

    private List<CarNorth> carsNorth;
    private List<CarEast> carsEast;
    private List<CarSouth> carsSouth;
    private List<CarWest> carsWest;

    private Semaphore capacitySemaphore;
    private AtomicInteger currentDirection;
    private List<Car> waitingCars;
    private ReentrantLock directionLock;
    private int maxCarsPerTurn;
    
    // Variables para control de ejecución
    private AtomicBoolean simulationRunning;
    private AtomicBoolean simulationStopped;
    private Thread coordinatorThread;
    private List<Thread> activeCarThreads;
    
    private Image s1 = loadImage("/uptc/edu/co/s1.png");
    private Image s2 = loadImage("/uptc/edu/co/s2.png");
    private Image s3 = loadImage("/uptc/edu/co/s3.png");

    private Image loadImage(String path) {
        try {
            URL resourceUrl = getClass().getResource(path);
            if (resourceUrl == null) {
                System.err.println("Recurso no encontrado: " + path);
                return null;
            }
            System.out.println("Cargando imagen desde: " + resourceUrl.toString());
            return new Image(resourceUrl.toString());
        } catch (Exception e) {
            System.err.println("Error loading image " + path + ": " + e.getMessage());
            return null;
        }
    }

    @FXML
    void options(ActionEvent event) {
        optionsVBox.setVisible(!optionsVBox.isVisible());
    }

    @FXML
    void start(ActionEvent event) {
        // Inicializar variables de control
        setupButton.setVisible(false);
        simulationRunning = new AtomicBoolean(true);
        simulationStopped = new AtomicBoolean(false);
        activeCarThreads = new ArrayList<>();
        carImageViews = new ArrayList<>();
        
        optionsVBox.setVisible(false);
        stopButton.setVisible(true);
        resetButton.setVisible(false);
        
        int permits = Integer.parseInt(permitsTextField.getText());
        maxCarsPerTurn = permits;
        
        capacitySemaphore = new Semaphore(permits, true);
        currentDirection = new AtomicInteger(0);
        waitingCars = new ArrayList<>();
        directionLock = new ReentrantLock();
        
        carsNorth = new ArrayList<>();
        carsEast = new ArrayList<>();
        carsSouth = new ArrayList<>();
        carsWest = new ArrayList<>();
        
        // Cargar imágenes de carros
        car1Image = loadImage("/uptc/edu/co/car1.png");
        car2Image = loadImage("/uptc/edu/co/car2.png");
        car3Image = loadImage("/uptc/edu/co/car3.png");
        car4Image = loadImage("/uptc/edu/co/car4.png");
        car5Image = loadImage("/uptc/edu/co/car5.png");
        car6Image = loadImage("/uptc/edu/co/car6.png");
        car7Image = loadImage("/uptc/edu/co/car7.png");
        car8Image = loadImage("/uptc/edu/co/car8.png");
        car9Image = loadImage("/uptc/edu/co/car9.png");
        car10Image = loadImage("/uptc/edu/co/car10.png");

        createAndQueueCars();
        startDirectionCoordinator();
    }

    @FXML
    void stop(ActionEvent event) {
        if (simulationRunning != null) {
            simulationRunning.set(false);
            simulationStopped.set(true);
        }
        resetButton.setVisible(true);
        stopButton.setVisible(false);
        
        // Poner todos los semáforos en amarillo (precaución)
        setAllSemaphoresYellow();
        
        System.out.println("=== SIMULACIÓN DETENIDA ===");
    }

    @FXML
    void reset(ActionEvent event) {
        // Detener completamente la simulación
        stopSimulation();
        
        // Limpiar toda la simulación
        clearSimulation();
        
        // Restablecer la interfaz
        resetUI();
        setupButton.setVisible(true);
        System.out.println("=== SIMULACIÓN REINICIADA ===");
    }

    private void stopSimulation() {
        // Detener el hilo coordinador
        if (coordinatorThread != null && coordinatorThread.isAlive()) {
            coordinatorThread.interrupt();
        }
        
        // Detener todos los hilos de carros activos
        for (Thread carThread : activeCarThreads) {
            if (carThread != null && carThread.isAlive()) {
                carThread.interrupt();
            }
        }
        activeCarThreads.clear();
        
        simulationRunning.set(false);
        simulationStopped.set(true);
        setupButton.setVisible(false);
    }

    private void clearSimulation() {
        // Limpiar listas
        if (waitingCars != null) waitingCars.clear();
        if (carsNorth != null) carsNorth.clear();
        if (carsEast != null) carsEast.clear();
        if (carsSouth != null) carsSouth.clear();
        if (carsWest != null) carsWest.clear();
        
        // Limpiar solo los ImageView de carros usando nuestra lista de tracking
        javafx.application.Platform.runLater(() -> {
            if (carImageViews != null) {
                mainAnchorPane.getChildren().removeAll(carImageViews);
                carImageViews.clear();
            }
        });
    }

    private void resetUI() {
        javafx.application.Platform.runLater(() -> {
            // Restablecer contadores
            northCarsCount = 0;
            eastCarsCount = 0;
            southCarsCount = 0;
            westCarsCount = 0;
            
            northCarsCountLabel.setText("North Cars Count: 0");
            eastCarsCountLabel.setText("East Cars Count: 0");
            southCarsCountLabel.setText("South Cars Count: 0");
            westCarsCountLabel.setText("West Cars Count: 0");
            
            // Poner semáforos en rojo
            semaphoreNorth.setImage(s1);
            semaphoreEast.setImage(s1);
            semaphoreSouth.setImage(s1);
            semaphoreWest.setImage(s1);
            
            // Mostrar opciones y ocultar botones de control
            optionsVBox.setVisible(false);
            stopButton.setVisible(false);
            resetButton.setVisible(false);
        });
    }

    private void setAllSemaphoresYellow() {
        javafx.application.Platform.runLater(() -> {
            semaphoreNorth.setImage(s2);
            semaphoreEast.setImage(s2);
            semaphoreSouth.setImage(s2);
            semaphoreWest.setImage(s2);
        });
    }

    private void createAndQueueCars() {
        // Crear lista de todas las imágenes de carros disponibles
        List<Image> carImages = new ArrayList<>();
        carImages.add(car1Image);
        carImages.add(car2Image);
        carImages.add(car3Image);
        carImages.add(car4Image);
        carImages.add(car5Image);
        carImages.add(car6Image);
        carImages.add(car7Image);
        carImages.add(car8Image);
        carImages.add(car9Image);
        carImages.add(car10Image);
        
        // Norte
        for(int i = 0; i < Integer.parseInt(northCarsTextField.getText()); i++) {
            Image randomCarImage = getRandomCarImage(carImages);
            CarNorth car = new CarNorth(randomCarImage, semaphoreNorth);
            carsNorth.add(car);
            waitingCars.add(car);
            mainAnchorPane.getChildren().add(car.getImageView());
            carImageViews.add(car.getImageView());
        }
        
        // Este
        for(int i = 0; i < Integer.parseInt(eastCarsTextField.getText()); i++) {
            Image randomCarImage = getRandomCarImage(carImages);
            CarEast car = new CarEast(randomCarImage, semaphoreEast);
            carsEast.add(car);
            waitingCars.add(car);
            mainAnchorPane.getChildren().add(car.getImageView());
            carImageViews.add(car.getImageView());
        }
        
        // Sur
        for(int i = 0; i < Integer.parseInt(southCarsTextField.getText()); i++) {
            Image randomCarImage = getRandomCarImage(carImages);
            CarSouth car = new CarSouth(randomCarImage, semaphoreSouth);
            carsSouth.add(car);
            waitingCars.add(car);
            mainAnchorPane.getChildren().add(car.getImageView());
            carImageViews.add(car.getImageView());
        }
        
        // Oeste
        for(int i = 0; i < Integer.parseInt(westCarsTextField.getText()); i++) {
            Image randomCarImage = getRandomCarImage(carImages);
            CarWest car = new CarWest(randomCarImage, semaphoreWest);
            carsWest.add(car);
            waitingCars.add(car);
            mainAnchorPane.getChildren().add(car.getImageView());
            carImageViews.add(car.getImageView());
        }
    }

    private Image getRandomCarImage(List<Image> carImages) {
        int randomIndex = (int) (Math.random() * carImages.size());
        return carImages.get(randomIndex);
    }

    private void startDirectionCoordinator() {
        coordinatorThread = new Thread(() -> {
            try {
                while (!allCarsProcessed() && simulationRunning.get()) {
                    // Verificar si la simulación fue detenida
                    if (simulationStopped.get()) {
                        System.out.println("Coordinador detenido");
                        break;
                    }
                    
                    directionLock.lock();
                    try {
                        int direction = currentDirection.get();
                        System.out.println("=== TURNO PARA: " + getDirectionName(direction) + " ===");
                        
                        allowLimitedCarsToPass(direction);
                        
                        currentDirection.set((direction + 1) % 4);
                        
                        Thread.sleep(1000);
                        
                    } finally {
                        directionLock.unlock();
                    }
                }
                
                if (!simulationStopped.get()) {
                    System.out.println("=== TODOS LOS CARROS HAN CRUZADO ===");
                    resetButton.setVisible(true);
                    stopButton.setVisible(false);
                }
                
            } catch (InterruptedException e) {
                System.out.println("Coordinador interrumpido");
                Thread.currentThread().interrupt();
            }
        });
        coordinatorThread.setDaemon(true);
        coordinatorThread.start();
    }

    private void allowLimitedCarsToPass(int direction) {
        List<Car> directionCars = getCarsByDirection(direction);
        List<Car> carsToProcess = new ArrayList<>();
        
        for (Car car : directionCars) {
            if (waitingCars.contains(car) && carsToProcess.size() < maxCarsPerTurn) {
                carsToProcess.add(car);
            }
        }
        
        if (carsToProcess.isEmpty()) {
            System.out.println("No hay carros esperando en " + getDirectionName(direction));
            return;
        }
        
        System.out.println("Procesando " + carsToProcess.size() + " carros de " + getDirectionName(direction));
        
        setSemaphoreGreen(direction);
        
        List<Thread> carThreads = new ArrayList<>();
        for (Car car : carsToProcess) {
            if (!simulationRunning.get()) break;
            
            Thread carThread = new Thread(new CarThread(car));
            carThreads.add(carThread);
            activeCarThreads.add(carThread);
            carThread.start();
            
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        setSemaphoreYellow(direction);
        try { 
            Thread.sleep(500); 
        } catch (InterruptedException e) { 
            Thread.currentThread().interrupt();
        }
        
        waitForCarsToComplete(carThreads);
        
        setSemaphoreRed(direction);
        
        // Remover hilos completados de la lista activa
        activeCarThreads.removeAll(carThreads);
    }

    private void waitForCarsToComplete(List<Thread> carThreads) {
        for (Thread thread : carThreads) {
            try {
                if (simulationRunning.get()) {
                    thread.join(5000); // Timeout de 5 segundos para evitar bloqueos
                } else {
                    thread.interrupt(); // Interrumpir si la simulación se detuvo
                }
            } catch (InterruptedException e) {
                thread.interrupt();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private boolean allCarsProcessed() {
        return waitingCars.isEmpty();
    }
    
    private List<Car> getCarsByDirection(int direction) {
        switch (direction) {
            case 0: return new ArrayList<>(carsNorth);
            case 1: return new ArrayList<>(carsEast);
            case 2: return new ArrayList<>(carsSouth);
            case 3: return new ArrayList<>(carsWest);
            default: return new ArrayList<>();
        }
    }
    
    private String getDirectionName(int direction) {
        switch (direction) {
            case 0: return "NORTE";
            case 1: return "ESTE";
            case 2: return "SUR";
            case 3: return "OESTE";
            default: return "DESCONOCIDO";
        }
    }

    private void setSemaphoreGreen(int direction) {
        javafx.application.Platform.runLater(() -> {
            switch (direction) {
                case 0: 
                    semaphoreNorth.setImage(s3);
                    System.out.println("SEMÁFORO NORTE: VERDE");
                    break;
                case 1: 
                    semaphoreEast.setImage(s3);
                    System.out.println("SEMÁFORO ESTE: VERDE");
                    break;
                case 2: 
                    semaphoreSouth.setImage(s3);
                    System.out.println("SEMÁFORO SUR: VERDE");
                    break;
                case 3: 
                    semaphoreWest.setImage(s3);
                    System.out.println("SEMÁFORO OESTE: VERDE");
                    break;
            }
        });
    }
    
    private void setSemaphoreRed(int direction) {
        javafx.application.Platform.runLater(() -> {
            switch (direction) {
                case 0: 
                    semaphoreNorth.setImage(s1);
                    System.out.println("SEMÁFORO NORTE: ROJO");
                    break;
                case 1: 
                    semaphoreEast.setImage(s1);
                    System.out.println("SEMÁFORO ESTE: ROJO");
                    break;
                case 2: 
                    semaphoreSouth.setImage(s1);
                    System.out.println("SEMÁFORO SUR: ROJO");
                    break;
                case 3: 
                    semaphoreWest.setImage(s1);
                    System.out.println("SEMÁFORO OESTE: ROJO");
                    break;
            }
        });
    }

    private void setSemaphoreYellow(int direction) {
        javafx.application.Platform.runLater(() -> {
            switch (direction) {
                case 0: semaphoreNorth.setImage(s2); break;
                case 1: semaphoreEast.setImage(s2); break;
                case 2: semaphoreSouth.setImage(s2); break;
                case 3: semaphoreWest.setImage(s2); break;
            }
        });
    }

    private class CarThread implements Runnable {
        private final Car car;

        public CarThread(Car car) {
            this.car = car;
        }

        @Override
        public void run() {
            try {
                // Verificar si la simulación fue detenida
                if (!simulationRunning.get()) {
                    return;
                }
                
                System.out.println(car.getName() + " is waiting for capacity.");
                
                capacitySemaphore.acquire();
                
                // Verificar nuevamente después de adquirir el permiso
                if (!simulationRunning.get()) {
                    capacitySemaphore.release();
                    return;
                }
                
                System.out.println(car.getName() + " has entered the intersection.");
                
                moveCarAnimation();
                
                System.out.println(car.getName() + " has exited the intersection.");
                
                capacitySemaphore.release();
                
                if (simulationRunning.get()) {
                    javafx.application.Platform.runLater(() -> {
                        waitingCars.remove(car);
                        mainAnchorPane.getChildren().remove(car.getImageView());
                        countCars();
                    });
                }

            } catch (InterruptedException e) {
                System.out.println(car.getName() + " interrumpido");
                // Liberar el permiso si fue adquirido
                if (capacitySemaphore.availablePermits() < maxCarsPerTurn) {
                    capacitySemaphore.release();
                }
                Thread.currentThread().interrupt();
            }
        }
        
        private void moveCarAnimation() {
            int sleepTime = Math.max(20, 50 - (capacitySemaphore.availablePermits() * 5));
            
            for (int i = 0; i < 50; i++) {
                // Verificar en cada iteración si la simulación fue detenida
                if (!simulationRunning.get() || Thread.currentThread().isInterrupted()) {
                    break;
                }
                
                try {
                    Thread.sleep(sleepTime); 
                    javafx.application.Platform.runLater(() -> {
                        if (simulationRunning.get()) {
                            car.move();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        private void countCars() {
            switch (car.getName()) {
                case "Car North" -> northCarsCountLabel.setText("North Cars Count: " + ++northCarsCount);
                case "Car East" -> eastCarsCountLabel.setText("East Cars Count: " + ++eastCarsCount);
                case "Car South" -> southCarsCountLabel.setText("South Cars Count: " + ++southCarsCount);
                case "Car West" -> westCarsCountLabel.setText("West Cars Count: " + ++westCarsCount);
                default -> System.out.println("Unknown car direction.");
            }
        }
    }
}