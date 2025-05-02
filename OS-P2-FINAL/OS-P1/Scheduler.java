import java.io.*;
import java.util.*;

// Class to represent a process
class Process {
    int pid, arrivalTime, burstTime, priority;
    int startTime, completionTime, waitingTime, turnaroundTime;

    Process(int pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }
}

public class Scheduler {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Menu system
        System.out.println("==== OS Simulation ====");
        System.out.println("1. Process Scheduling (FCFS & SJF)");
        System.out.println("2. Memory Allocation (First-Fit, Best-Fit, Worst-Fit)");
        System.out.println("3. Paging + Page Replacement (FIFO, LRU)");
        System.out.print("Choose option: ");
        int option = scanner.nextInt();

        if (option == 1) {
            // Load process list from file
            List<Process> processes = readProcesses("processes.txt");

            System.out.println("\n--- First-Come, First-Served (FCFS) ---");
            simulateFCFS(deepCopy(processes));

            System.out.println("\n--- Shortest Job First (SJF) ---");
            simulateSJF(deepCopy(processes));

        } else if (option == 2) {
            // Memory Allocation Menu
            System.out.println("Enter number of memory blocks: ");
            int m = scanner.nextInt();
            int[] memoryBlocks = new int[m];

            System.out.println("Enter block sizes:");
            for (int i = 0; i < m; i++) memoryBlocks[i] = scanner.nextInt();

            System.out.println("Enter number of processes: ");
            int p = scanner.nextInt();
            int[] processSizes = new int[p];

            System.out.println("Enter process sizes:");
            for (int i = 0; i < p; i++) processSizes[i] = scanner.nextInt();

            System.out.println("\nChoose allocation strategy:");
            System.out.println("1. First-Fit");
            System.out.println("2. Best-Fit");
            System.out.println("3. Worst-Fit");
            int strategy = scanner.nextInt();

            // Apply chosen strategy
            switch (strategy) {
                case 1:
                    firstFit(memoryBlocks.clone(), processSizes);
                    break;
                case 2:
                    bestFit(memoryBlocks.clone(), processSizes);
                    break;
                case 3:
                    worstFit(memoryBlocks.clone(), processSizes);
                    break;
                default:
                    System.out.println("Invalid strategy.");
            }

        } else if (option == 3) {
            // Page Replacement Menu
            System.out.print("Enter number of frames: ");
            int frames = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter reference string (space-separated): ");
            String[] refStr = scanner.nextLine().trim().split("\\s+");
            int[] referenceString = Arrays.stream(refStr).mapToInt(Integer::parseInt).toArray();

            System.out.println("Choose Page Replacement Strategy:");
            System.out.println("1. FIFO");
            System.out.println("2. LRU");
            int choice = scanner.nextInt();

            // Apply chosen strategy
            if (choice == 1) {
                fifoPaging(referenceString, frames);
            } else if (choice == 2) {
                lruPaging(referenceString, frames);
            } else {
                System.out.println("Invalid choice.");
            }

        } else {
            System.out.println("Invalid choice.");
        }
    }

    // Load processes from a text file
    public static List<Process> readProcesses(String filename) throws IOException {
        List<Process> processList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine(); // skip header line
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.trim().split("\\s+");
            int pid = Integer.parseInt(parts[0]);
            int arrival = Integer.parseInt(parts[1]);
            int burst = Integer.parseInt(parts[2]);
            int priority = Integer.parseInt(parts[3]);
            processList.add(new Process(pid, arrival, burst, priority));
        }
        reader.close();
        return processList;
    }

    // FCFS Scheduling
    public static void simulateFCFS(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime)); // Sort by arrival time
        int currentTime = 0;
        List<String> ganttChart = new ArrayList<>();

        for (Process p : processes) {
            if (currentTime < p.arrivalTime) currentTime = p.arrivalTime; // wait if process hasn't arrived
            p.startTime = currentTime;
            p.completionTime = currentTime + p.burstTime;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.startTime - p.arrivalTime;
            currentTime += p.burstTime;
            ganttChart.add("P" + p.pid);
        }

        printGanttChart(processes, ganttChart);
        printStats(processes);
    }

    // SJF (non-preemptive) Scheduling
    public static void simulateSJF(List<Process> processes) {
        List<Process> completed = new ArrayList<>();
        int currentTime = 0;
        List<String> ganttChart = new ArrayList<>();

        while (completed.size() < processes.size()) {
            // Find all processes that have arrived and are not completed
            List<Process> readyQueue = new ArrayList<>();
            for (Process p : processes) {
                if (!completed.contains(p) && p.arrivalTime <= currentTime) {
                    readyQueue.add(p);
                }
            }

            if (readyQueue.isEmpty()) {
                currentTime++; // idle time
                continue;
            }

            // Choose shortest burst time among ready processes
            readyQueue.sort(Comparator.comparingInt(p -> p.burstTime));
            Process current = readyQueue.get(0);

            current.startTime = currentTime;
            current.completionTime = currentTime + current.burstTime;
            current.turnaroundTime = current.completionTime - current.arrivalTime;
            current.waitingTime = current.startTime - current.arrivalTime;
            currentTime += current.burstTime;

            ganttChart.add("P" + current.pid);
            completed.add(current);
        }

        printGanttChart(completed, ganttChart);
        printStats(completed);
    }

    // Display Gantt chart
    public static void printGanttChart(List<Process> processes, List<String> chart) {
        System.out.print("\nGantt Chart:\n| ");
        for (String s : chart) System.out.print(s + " | ");
        System.out.println();

        int time = 0;
        System.out.print("0");
        for (Process p : processes) {
            time = Math.max(time, p.startTime) + p.burstTime;
            System.out.print("   " + time);
        }
        System.out.println();
    }

    // Print waiting time and turnaround time for each process
    public static void printStats(List<Process> processes) {
        double totalWT = 0, totalTAT = 0;
        System.out.println("\nPID\tAT\tBT\tWT\tTAT");
        for (Process p : processes) {
            System.out.printf("%d\t%d\t%d\t%d\t%d\n", p.pid, p.arrivalTime, p.burstTime, p.waitingTime, p.turnaroundTime);
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }
        System.out.printf("\nAverage Waiting Time: %.2f\n", totalWT / processes.size());
        System.out.printf("Average Turnaround Time: %.2f\n", totalTAT / processes.size());
    }

    // Create a copy of process list for multiple algorithm testing
    public static List<Process> deepCopy(List<Process> original) {
        List<Process> copy = new ArrayList<>();
        for (Process p : original) {
            copy.add(new Process(p.pid, p.arrivalTime, p.burstTime, p.priority));
        }
        return copy;
    }

    // ===== Memory Allocation Algorithms =====

    // First-Fit: Allocates to first available block
    static void firstFit(int[] blocks, int[] processes) {
        boolean[] allocated = new boolean[blocks.length];

        for (int i = 0; i < processes.length; i++) {
            boolean placed = false;
            for (int j = 0; j < blocks.length; j++) {
                if (!allocated[j] && blocks[j] >= processes[i]) {
                    System.out.println("Process " + (i + 1) + " of size " + processes[i] + " allocated to Block " + (j + 1) + " (" + blocks[j] + " KB)");
                    allocated[j] = true;
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                System.out.println("Process " + (i + 1) + " of size " + processes[i] + " cannot be allocated");
            }
        }
    }

    // Best-Fit: Allocates to smallest block that fits
    static void bestFit(int[] blocks, int[] processes) {
        boolean[] allocated = new boolean[blocks.length];

        for (int i = 0; i < processes.length; i++) {
            int bestIdx = -1;
            for (int j = 0; j < blocks.length; j++) {
                if (!allocated[j] && blocks[j] >= processes[i]) {
                    if (bestIdx == -1 || blocks[j] < blocks[bestIdx]) {
                        bestIdx = j;
                    }
                }
            }

            if (bestIdx != -1) {
                System.out.println("Process " + (i + 1) + " of size " + processes[i] + " allocated to Block " + (bestIdx + 1) + " (" + blocks[bestIdx] + " KB)");
                allocated[bestIdx] = true;
            } else {
                System.out.println("Process " + (i + 1) + " of size " + processes[i] + " cannot be allocated");
            }
        }
    }

    // Worst-Fit: Allocates to largest block that fits
    static void worstFit(int[] blocks, int[] processes) {
        boolean[] allocated = new boolean[blocks.length];

        for (int i = 0; i < processes.length; i++) {
            int worstIdx = -1;
            for (int j = 0; j < blocks.length; j++) {
                if (!allocated[j] && blocks[j] >= processes[i]) {
                    if (worstIdx == -1 || blocks[j] > blocks[worstIdx]) {
                        worstIdx = j;
                    }
                }
            }

            if (worstIdx != -1) {
                System.out.println("Process " + (i + 1) + " of size " + processes[i] + " allocated to Block " + (worstIdx + 1) + " (" + blocks[worstIdx] + " KB)");
                allocated[worstIdx] = true;
            } else {
                System.out.println("Process " + (i + 1) + " of size " + processes[i] + " cannot be allocated");
            }
        }
    }

    // ===== Paging Algorithms =====

    // FIFO Page Replacement
    static void fifoPaging(int[] pages, int frameCount) {
        Set<Integer> memory = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        int faults = 0;

        for (int page : pages) {
            if (!memory.contains(page)) {
                if (memory.size() == frameCount) {
                    int removed = queue.poll(); // Evict the oldest page
                    memory.remove(removed);
                    System.out.println("Page " + page + " → Replaces Page " + removed + " (miss)");
                } else {
                    System.out.println("Page " + page + " → Loaded (miss)");
                }
                memory.add(page);
                queue.add(page);
                faults++;
            } else {
                System.out.println("Page " + page + " → Already in memory (hit)");
            }
        }

        System.out.println("Total Page Faults: " + faults);
    }

    // LRU Page Replacement
    static void lruPaging(int[] pages, int frameCount) {
        Set<Integer> memory = new HashSet<>();
        Map<Integer, Integer> lastUsed = new HashMap<>();
        int faults = 0;

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];

            if (!memory.contains(page)) {
                if (memory.size() == frameCount) {
                    // Find LRU page
                    int lruPage = -1, oldest = Integer.MAX_VALUE;
                    for (int p : memory) {
                        if (lastUsed.get(p) < oldest) {
                            oldest = lastUsed.get(p);
                            lruPage = p;
                        }
                    }
                    memory.remove(lruPage);
                    System.out.println("Page " + page + " → Replaces Page " + lruPage + " (miss)");
                } else {
                    System.out.println("Page " + page + " → Loaded (miss)");
                }
                memory.add(page);
                faults++;
            } else {
                System.out.println("Page " + page + " → Already in memory (hit)");
            }

            lastUsed.put(page, i); // Track last use time
        }

        System.out.println("Total Page Faults: " + faults);
    }
}
