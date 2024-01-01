import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

// 定义缓存行的状态
enum CacheLineState {
    INVALID,   // 失效
    SHARED,    // 共享
    EXCLUSIVE  // 独占
}

// 定义主存类
class MainMemory {
    public final int size;
    private final String[] data;

    public MainMemory(int size) {
        this.size = size;
        data = new String[size];
        for (int i = 0; i < size; i++) {
            data[i] = String.valueOf(i);
        }
    }

    public void clean() {
        for (int i = 0; i < size; i++) {
            data[i] = String.valueOf(i);
        }
    }

    public String readData(int address) {
        return data[address];
    }

    public void writeData(int address, String newData) {
        data[address] = newData;
    }
}

// 定义处理器类
class Processor {
    public final static int cacheNum = 4;
    public static Vector<String> messages = new Vector<>();
    private final String id;
    private final CacheLineState[] cacheStates; // 每个缓存行的状态
    private final String[] cacheData;           // 每个缓存行的数据
    private final int[] cacheAddress;           // 用于存储每个缓存行中的主存地址
    private final MainMemory mainMemory;

    public Processor(String id, MainMemory mainMemory) {
        this.id = id;
        this.cacheStates = new CacheLineState[Processor.cacheNum]; // 每个处理器有4个缓存行
        this.cacheData = new String[Processor.cacheNum];            // 数据缓存
        this.cacheAddress = new int[Processor.cacheNum];
        this.mainMemory = mainMemory;
        for (int i = 0; i < Processor.cacheNum; i++) {
            cacheStates[i] = CacheLineState.INVALID;
            cacheData[i] = "";
            cacheAddress[i] = mainMemory.size;
        }
    }

    public static void addMessage(String string) {
        System.out.println(string);
        Processor.messages.add(string);
    }

    public void clean() {
        for (int i = 0; i < Processor.cacheNum; i++) {
            cacheStates[i] = CacheLineState.INVALID;
            cacheData[i] = "";
            cacheAddress[i] = mainMemory.size;
        }
    }

    public String getId() {
        return id;
    }

    public CacheLineState getCacheStates(int index) {
        return cacheStates[index];
    }

    public String getCacheData(int index) {
        return cacheData[index];
    }

    public String readData(int address) {
        int cacheIndex = address % cacheNum; // 使用取余操作确定缓存行索引
        if (checkHit(address)) {
            addMessage("CPU:" + id + " 读:" + address + "  cache命中");
        } else {
            addMessage("CPU:" + id + " 读:" + address + "  cache未命中 读主存...");
            checkCacheStatus(address, "r");
            updateCache(address, mainMemory.readData(address), CacheLineState.SHARED);
        }
        addMessage("CPU:" + id + " 得到结果:" + cacheData[cacheIndex]);
        addMessage("---------------==========+==========---------------");
        return cacheData[cacheIndex];
    }

    public void writeData(int address, String newData) {
        if (checkHit(address)) {
            addMessage("CPU:" + id + " 写:" + address + " cache命中");
        } else {
            addMessage("CPU:" + id + " 写:" + address + " cache未命中");
        }
        checkCacheStatus(address, "w");
        updateCache(address, newData, CacheLineState.EXCLUSIVE);
        addMessage("---------------==========+==========---------------");
    }

    private void checkCacheStatus(int address, String fun) {
        int cacheIndex = address % cacheNum; // 使用取余操作确定缓存行索引
        for (Processor p : MultiProcessor.processors) {
            if (!this.equals(p) && p.checkHit(address)) {
                if (p.cacheStates[cacheIndex] == CacheLineState.SHARED && fun.equals("w")) {
                    p.updateCache(address, "", CacheLineState.INVALID);
                }
                if (p.cacheStates[cacheIndex] == CacheLineState.EXCLUSIVE) {
                    p.writeBack(address);
                    p.updateCache(address, p.getCacheData(cacheIndex), CacheLineState.SHARED);
                    return;
                }
            } else {
                if (cacheStates[cacheIndex] == CacheLineState.EXCLUSIVE) {
                    writeBack(cacheAddress[cacheIndex]);
                    updateCache(cacheAddress[cacheIndex], cacheData[cacheIndex], CacheLineState.SHARED);
                }
            }
        }
    }

    private void writeBack(int address) {
        int cacheIndex = address % cacheNum; // 使用取余操作确定缓存行索引
        addMessage("CPU:" + id + " 写回主存:" + address);
        mainMemory.writeData(address, cacheData[cacheIndex]);
    }

    public void updateCache(int address, String newData, CacheLineState newState) {
        int cacheIndex = address % cacheNum; // 使用取余操作确定缓存行索引
        if (newState == CacheLineState.INVALID) {
            addMessage("CPU:" + id + " Cache:" + cacheIndex + " 作废" + " 状态: " + newState);
            cacheData[cacheIndex] = "";
            cacheStates[cacheIndex] = CacheLineState.INVALID;
            cacheAddress[cacheIndex] = mainMemory.size;
            return;
        }
        addMessage("CPU:" + id + " Cache:" + cacheIndex + " 更新主存地址:" + address + " 状态: " + newState);
        cacheData[cacheIndex] = newData;
        cacheStates[cacheIndex] = newState;
        cacheAddress[cacheIndex] = address;
    }

    public boolean checkHit(int address) {
        int cacheIndex = address % cacheNum; // 使用取余操作确定缓存行索引
        // 处理缓存命中的逻辑
        return cacheAddress[cacheIndex] == address;
    }
}

public class MultiProcessor extends JFrame {
    static List<Processor> processors = new ArrayList<>();
    MainMemory mainMemory;
    Processor p1;
    Processor p2;
    Processor p3;
    Processor p4;
    int processorSize;
    JTextField[] dataFields;
    JLabel[][] cacheLabels;
    JLabel[] memoryLabels;
    JComboBox<Integer>[] addressBoxes;
    JComboBox<String>[] operationComboBoxes;
    JList<String> messageList;

    public MultiProcessor() {
        mainMemory = new MainMemory(32);
        p1 = new Processor("A", mainMemory);
        p2 = new Processor("B", mainMemory);
        p3 = new Processor("C", mainMemory);
        p4 = new Processor("D", mainMemory);
        processors.add(p1);
        processors.add(p2);
        processors.add(p3);
        processors.add(p4);
        processorSize = processors.size();

        // 设置 CPU 部分的相关组件
        cacheLabels = new JLabel[processorSize][Processor.cacheNum];
        dataFields = new JTextField[processorSize];
        addressBoxes = new JComboBox[processorSize];
        operationComboBoxes = new JComboBox[processorSize];
        for (int i = 0; i < processorSize; i++) {
            Processor p = processors.get(i);
            dataFields[i] = new JTextField(1);
            addressBoxes[i] = new JComboBox<>(new DefaultComboBoxModel<>(generateNumbers(0, 31)));
            operationComboBoxes[i] = new JComboBox<>(new String[]{"Read", "Write"});
            for (int j = 0; j < 4; j++) {
                cacheLabels[i][j] = new JLabel("Cache " + j + " 数据: " + p.getCacheData(j) + " 状态: " + p.getCacheStates(j));
            }
        }

        // 重新设置内存部分的相关组件
        memoryLabels = new JLabel[32];
        for (int i = 0; i < 32; i++) {
            memoryLabels[i] = new JLabel("Block " + i + " 数据: " + mainMemory.readData(i));
        }
        setTitle("多Cache一致性模拟器--监听法");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel functionPanel = new JPanel();
        functionPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());

        JButton resetButton = new JButton("复位");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                init();
            }
        });

        JButton doButton = new JButton("执行指令文件");
        doButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String filePath = "Commands.txt";

                try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        processLine(line);
                    }
                } catch (IOException ex) {
                    Processor.addMessage("无指令文件");
                    Processor.addMessage("---------------==========+==========---------------");
                }
                update();
            }
        });

        buttonPanel.add(resetButton, BorderLayout.NORTH);
        buttonPanel.add(doButton, BorderLayout.SOUTH);

        functionPanel.add(buttonPanel, BorderLayout.NORTH);

        // CPUs
        JPanel[] cpuPanels = new JPanel[processorSize];
        JButton[] startButtons = new JButton[processorSize];
        JPanel cpuContainer = new JPanel();
        cpuContainer.setLayout(new GridLayout(2, processorSize / 2));

        for (int i = 0; i < processorSize; i++) {
            Processor p = processors.get(i);
            cpuPanels[i] = new JPanel();
            cpuPanels[i].setLayout(new BoxLayout(cpuPanels[i], BoxLayout.Y_AXIS));
            cpuPanels[i].setBorder(BorderFactory.createTitledBorder("CPU " + p.getId()));

            cpuPanels[i].add(dataFields[i]);
            cpuPanels[i].add(addressBoxes[i]);
            cpuPanels[i].add(operationComboBoxes[i]);
            startButtons[i] = new JButton("运行");
            int finalI = i;
            startButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int address = (int) addressBoxes[finalI].getSelectedItem();
                    String operationText = (String) operationComboBoxes[finalI].getSelectedItem();
                    if (operationText.equals("Read")) {
                        p.readData(address);
                    } else if (operationText.equals("Write")) {
                        p.writeData(address, dataFields[finalI].getText());
                    }
                    update();
                }
            });
            cpuPanels[i].add(startButtons[i]);
            JPanel cachePanel = new JPanel();
            cachePanel.setBorder(BorderFactory.createTitledBorder("Caches"));
            cachePanel.setLayout(new GridLayout(4, 1));
            for (int j = 0; j < 4; j++) {
                cachePanel.add(cacheLabels[i][j]);
            }
            cpuPanels[i].add(cachePanel);
            cpuContainer.add(cpuPanels[i]);
        }
        functionPanel.add(cpuContainer, BorderLayout.CENTER);

        // Memory
        JPanel memoryPanel = new JPanel();
        memoryPanel.setLayout(new GridLayout(8, 4));
        memoryPanel.setBorder(BorderFactory.createTitledBorder("存储器"));
        for (int i = 0; i < 32; i++) {
            memoryPanel.add(memoryLabels[i]);
        }
        functionPanel.add(memoryPanel, BorderLayout.SOUTH);
        add(functionPanel);

        messageList = new JList<>();
        JScrollPane scrollPane = new JScrollPane(messageList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("操作记录"));
        add(scrollPane, BorderLayout.EAST);
    }

    private static void processLine(String line) {
        char firstChar = line.charAt(0);
        String[] parts = line.split(" ", 4);

        switch (firstChar) {
            case 'r':
                if (parts.length > 2) {
                    String id = parts[1];
                    int address = Integer.parseInt(parts[2]);
                    try {
                        getProcessorById(id).readData(address);
                    } catch (NullPointerException ex) {
                        Processor.addMessage("错误的处理器ID");
                        Processor.addMessage("---------------==========+==========---------------");
                    }
                }
                break;
            case 'w':
                if (parts.length > 3) {
                    String id = parts[1];
                    int address = Integer.parseInt(parts[2]);
                    String data = parts[3];
                    try {
                        getProcessorById(id).writeData(address, data);
                    } catch (NullPointerException ex) {
                        Processor.addMessage("错误的处理器ID");
                        Processor.addMessage("---------------==========+==========---------------");
                    }
                }
                break;
            default:
                Processor.addMessage("错误的指令: " + line + "(r/w 处理器ID 地址 [数据])");
                Processor.addMessage("---------------==========+==========---------------");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultiProcessor().setVisible(true));
    }

    private static Processor getProcessorById(String id) {
        for (Processor p : processors) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    private Integer[] generateNumbers(int start, int end) {
        Integer[] numbers = new Integer[end - start + 1];
        for (int i = start; i <= end; i++) {
            numbers[i - start] = i;
        }
        return numbers;
    }

    private void init() {
        mainMemory.clean();
        for (Processor p : processors) {
            p.clean();
        }
        Processor.messages.clear();

        // 设置 CPU 部分的相关组件
        for (int i = 0; i < processorSize; i++) {
            Processor p = processors.get(i);
            dataFields[i].setText("");
            operationComboBoxes[i].setSelectedIndex(0);
            for (int j = 0; j < 4; j++) {
                cacheLabels[i][j].setText("Cache " + j + " 数据: " + p.getCacheData(j) + " 状态: " + p.getCacheStates(j));
            }
        }

        // 重新设置内存部分的相关组件
        for (int i = 0; i < 32; i++) {
            memoryLabels[i].setText("Block " + i + " 数据: " + mainMemory.readData(i));
        }

        messageList.setListData(new Vector<>());

        revalidate();
        repaint();
    }

    private void update() {
        for (int i = 0; i < processorSize; i++) {
            Processor p = processors.get(i);
            for (int j = 0; j < 4; j++) {
                cacheLabels[i][j].setText("Cache " + j + " 数据: " + p.getCacheData(j) + " 状态: " + p.getCacheStates(j));
            }
        }

        for (int i = 0; i < 32; i++) {
            memoryLabels[i].setText("Block " + i + " 数据: " + mainMemory.readData(i));
        }

        messageList.setListData(Processor.messages);

        revalidate();
        repaint();
    }
}