package farmersfridge;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

/**
 *
 * @author ChristyJeyaraj
 */
public class FarmersFridge {

    private static String FILENAME;
    private static final Double STARTLAT = 41.8851024;
    private static final Double STARTLON = -87.6618988;
    private static List<String> ALLROUTES;

    public static void main(String[] args) {
        // get the filename
        System.out.println("Enter the absolute path of the input file with extension: ");
        Scanner sc = new Scanner(System.in);
        FILENAME = sc.nextLine();
        sc.close();
        System.out.println();

        // read csv
        List<Kiosk> kioskList = readCsvFile(FILENAME);

        // cluster the kiosks into n clusters. n is the number of drivers
        int numberOfDriver = 2;
        Dataset[] clusters = getClusters(numberOfDriver, kioskList);
        List<List<Kiosk>> driversKioskLists = getDriverKioskLists(clusters, kioskList);

        for (int i = 0; i < driversKioskLists.size(); i++) {
            System.out.println("Driver " + (i + 1));
            getShortestRoute(driversKioskLists.get(i));
            System.out.println();
        }
    }

    private static void getShortestRoute(List<Kiosk> kioskList) {
        System.out.println("Total Kiosks: " + kioskList.size());

        ALLROUTES = new ArrayList();
        kioskList = addDepotToKioskList(kioskList);

        // reassign kiosk ids
        for (int i = 0; i < kioskList.size(); i++) {
            kioskList.get(i).Id = i;
        }

        // generate distance adj matrix
        Double[][] distanceMatrix = getDistanceMatrix(kioskList);

        // get kioskIds 
        int[] kioskIds = getKioskIds(kioskList);

        // get all possible routes
        System.out.println("Calculating routes...");
        findAllRoutes(kioskIds, 0);
        findMinCost(distanceMatrix, kioskList);
    }

    private static List<Kiosk> readCsvFile(String fileName) {
        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileName), ',', '"', 1);
            List<String[]> records = csvReader.readAll();
            List<Kiosk> kioskList = new ArrayList();
            for (int i = 0; i < records.size(); i++) {
                String[] record = records.get(i);
                Kiosk kiosk = new Kiosk((i + 1), record[0], record[1], Double.parseDouble(record[2]), Double.parseDouble(record[3]));
                kioskList.add(kiosk);
            }
            return kioskList;
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList();
        }
    }

    private static Dataset[] getClusters(int clusterSize, List<Kiosk> kioskList) {
        try {
            Dataset data = new DefaultDataset();
            for (int i = 0; i < kioskList.size(); i++) {
                Kiosk kiosk = kioskList.get(i);
                double[] instance = {kiosk.Latitude, kiosk.Longitude};
                data.add(new DenseInstance(instance, kiosk.Id));
            }

            Clusterer km = new KMeans(clusterSize, Integer.MAX_VALUE); // maximum number of iterations
            Dataset[] clusters = km.cluster(data);
            return clusters;
        } catch (Exception e) {
            System.err.println(e);
            return new Dataset[0];
        }
    }

    private static List<List<Kiosk>> getDriverKioskLists(Dataset[] kioskClusters, List<Kiosk> kioskList) {
        List<List<Kiosk>> driversKioskLists = new ArrayList();
        for (int i = 0; i < kioskClusters.length; i++) {
            Dataset currentKioskCluster = kioskClusters[i];
            List<Kiosk> driverKioskList = new ArrayList();
            for (int j = 0; j < currentKioskCluster.size(); j++) {
                Instance kiosk = currentKioskCluster.get(j);
                int kioskId = (int) kiosk.classValue();
                driverKioskList.add(kioskList.get(kioskId - 1));
            }
            driversKioskLists.add(driverKioskList);
        }
        
        Collections.sort(driversKioskLists, (a, b) -> a.size() < b.size() ? -1 : 1);
        return driversKioskLists;
    }

    private static List<Kiosk> addDepotToKioskList(List<Kiosk> kioskList) {
        Kiosk depot = new Kiosk(0, "Kitchen", "Lake and Racine", STARTLAT, STARTLON);
        kioskList.add(0, depot);
        return kioskList;
    }

    private static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        DecimalFormat df = new DecimalFormat("##.##");
        return (Double.parseDouble(df.format(dist)));
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private static Double[][] getDistanceMatrix(List<Kiosk> kioskList) {
        Double[][] distanceMatrix = new Double[kioskList.size()][kioskList.size()];
        for (int i = 0; i < kioskList.size(); i++) {
            for (int j = 0; j < kioskList.size(); j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0.0;
                } else {
                    distanceMatrix[i][j] = getDistance(kioskList.get(i).Latitude, kioskList.get(i).Longitude, kioskList.get(j).Latitude, kioskList.get(j).Longitude);
                }
            }
        }

//        for (int i = 0; i < distanceMatrix.length; i++) {
//            for (int j = 0; j < distanceMatrix[i].length; j++) {
//                System.out.print(distanceMatrix[i][j] + "\t");
//            }
//            System.out.println();
//        }
        return distanceMatrix;
    }

    private static int[] getKioskIds(List<Kiosk> kioskList) {
        int[] ids = new int[kioskList.size() - 1];

        // get ids leaving the depot
        int j = 1;
        for (int i = 0; i < ids.length; i++) {
            ids[i] = kioskList.get(j++).Id;
        }

//        for (int i = 0; i < ids.length; i++) {
//            System.out.print(ids[i] + " ");
//        }
//        System.out.println();
        return ids;
    }

    private static void findAllRoutes(int[] items, int capacity) {
        if (capacity == items.length) {
            String route = "";
            for (int i = 0; i < items.length; i++) {
                route = route + items[i] + "-";
            }
            route = "0-" + route + "0";
            if (!ALLROUTES.contains(new StringBuilder(route).reverse().toString())) {
                ALLROUTES.add(route);
//                System.out.println(route);
            }
        } else {
            for (int i = capacity; i < items.length; i++) {
                int temp = items[capacity];
                items[capacity] = items[i];
                items[i] = temp;

                findAllRoutes(items, capacity + 1);

                temp = items[capacity];
                items[capacity] = items[i];
                items[i] = temp;
            }
        }
    }

    private static void findMinCost(Double[][] distanceMatrix, List<Kiosk> kioskList) {
        Double minCost = Double.MAX_VALUE;
        int[] minRouteArray = new int[ALLROUTES.get(0).split("-").length];
        for (int i = 0; i < ALLROUTES.size(); i++) {
            String[] routesStringArray = ALLROUTES.get(i).split("-");
            int[] routesIntegerArray = new int[routesStringArray.length];
            for (int j = 0; j < routesStringArray.length; j++) {
                routesIntegerArray[j] = Integer.parseInt(routesStringArray[j]);
            }

            double cost = 0.0;
            for (int j = 0; j < routesIntegerArray.length - 1; j++) {
                cost = cost + distanceMatrix[routesIntegerArray[j]][routesIntegerArray[j + 1]];
            }
            if (cost < minCost) {
                minCost = cost;
                minRouteArray = routesIntegerArray;
            }
        }

        System.out.println("Best Route: ");
        System.out.println("   Distance: " + minCost + " miles");
        System.out.print("   Route: ");
        for (int i = 0; i < minRouteArray.length; i++) {
            System.out.print("[" + kioskList.get(minRouteArray[i]).Name + "]");
            if (i != (minRouteArray.length - 1)) {
                System.out.print(" -> ");
            }
        }
        System.out.println();
    }

}
