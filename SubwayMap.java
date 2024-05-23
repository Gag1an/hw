import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

class Station {
    String name;
    Set<String> lines;

    public Station(String name) {
        this.name = name;
        this.lines = new HashSet<>();
    }

    public void addLine(String line) {
        lines.add(line);
    }

    @Override
    public String toString() {
        return name + " " + lines;
    }
}

class LineSegment {
    String line;
    Station from;
    Station to;
    double distance;

    public LineSegment(String line, Station from, Station to, double distance) {
        this.line = line;
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return line + ": " + from.name + " -> " + to.name + " (" + distance + " km)";
    }
}

class SubwaySystem {
    Map<String, Station> stations;
    List<LineSegment> lineSegments;

    public SubwaySystem(String filePath) throws IOException {
        stations = new HashMap<>();
        lineSegments = new ArrayList<>();
        loadSubwayData(filePath);
    }

    private void loadSubwayData(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        String currentLine = "";

        while ((line = reader.readLine()) != null) {
            if (line.contains("号线站点间距")) {
                currentLine = line.split("号线")[0];
            } else if (line.contains("---")) {
                String[] parts = line.split("\t");
                String[] stationNames = parts[0].split("---");
                double distance = Double.parseDouble(parts[1]);

                addLineSegment(currentLine, stationNames[0], stationNames[1], distance);
            }
        }
        reader.close();
    }

    private void addLineSegment(String line, String from, String to, double distance) {
        Station fromStation = stations.computeIfAbsent(from, Station::new);
        Station toStation = stations.computeIfAbsent(to, Station::new);

        fromStation.addLine(line);
        toStation.addLine(line);

        lineSegments.add(new LineSegment(line, fromStation, toStation, distance));
        lineSegments.add(new LineSegment(line, toStation, fromStation, distance));
    }

    public Set<Station> getTransferStations() {
        Set<Station> transferStations = new HashSet<>();
        for (Station station : stations.values()) {
            if (station.lines.size() > 1) {
                transferStations.add(station);
            }
        }
        return transferStations;
    }

    public Map<Station, Double> getNearbyStations(String stationName, double maxDistance) {
        Station start = stations.get(stationName);
        if (start == null) {
            throw new IllegalArgumentException("站点不存在: " + stationName);
        }

        Map<Station, Double> nearbyStations = new HashMap<>();
        for (LineSegment segment : lineSegments) {
            if (segment.from.equals(start) && segment.distance <= maxDistance) {
                nearbyStations.put(segment.to, segment.distance);
            }
        }
        return nearbyStations;
    }

    public List<List<Station>> getAllPaths(String startName, String endName) {
        Station start = stations.get(startName);
        Station end = stations.get(endName);
        if (start == null || end == null) {
            throw new IllegalArgumentException("站点不存在: " + startName + " 或 " + endName);
        }
        List<List<Station>> paths = new ArrayList<>();
        findAllPaths(start, end, new HashSet<>(), new ArrayList<>(), paths);
        return paths;
    }
}


public class SubwayMap {
    public static void main(String[] args) {
        try {
            SubwaySystem subwaySystem = new SubwaySystem("C:\\Users\\33600\Desktop\\实验一\\WuHanSubway\\subway.txt");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("请选择");
                System.out.println("1. 输出地铁中转站");
                System.out.println("2. 查找附近的站点");
                System.out.println("3. 查找所有路径");
                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        Set<Station> transferStations = subwaySystem.getTransferStations();
                        System.out.println("Transfer Stations: " + transferStations);
                        break;

                    case 2:
                        System.out.println("输入站点名称:");
                        String stationName = scanner.nextLine();
                        System.out.println("输入距离:");
                        double distance = scanner.nextDouble();
                        scanner.nextLine(); // consume newline

                        try {
                            Map<Station, Double> nearbyStations = subwaySystem.getNearbyStations(stationName, distance);
                            System.out.println("Nearby Stations: " + nearbyStations);
                        } catch (IllegalArgumentException e) {
                            System.out.println(e.getMessage());
                        }
                        break;

                    case 3:
                        System.out.println("输入起点站:");
                        String startStation = scanner.nextLine();
                        System.out.println("输入终点站:");
                        String endStation = scanner.nextLine();

                        try {
                            List<List<Station>> allPaths = subwaySystem.getAllPaths(startStation, endStation);
                            System.out.println("All Paths: " + allPaths);
                        } catch (IllegalArgumentException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    default:
                        System.out.println("无效选择，请重新选择。");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
