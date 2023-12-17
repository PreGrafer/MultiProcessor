// 定义缓存行的状态
enum CacheLineState {
    INVALID, // 失效
    SHARED,  // 共享
    EXCLUSIVE, // 独占
    MODIFIED //修改
}

// 定义主存类
class MainMemory {
    private String[] data;

    public MainMemory(int size) {
        data = new String[size];
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
    private int id;
    private CacheLineState[] cacheStates; // 每个缓存行的状态
    private String[] cacheData; // 每个缓存行的数据
    private MainMemory mainMemory;

    public Processor(int id, MainMemory mainMemory) {
        this.id = id;
        this.cacheStates = new CacheLineState[4]; // 每个处理器有4个缓存行
        this.cacheData = new String[4]; // 数据缓存
        for (int i = 0; i < 4; i++) {
            cacheStates[i] = CacheLineState.INVALID;
            cacheData[i] = "";
        }
        this.mainMemory = mainMemory;
    }

    public String readData(int address) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        switch (cacheStates[cacheIndex]) {
            case INVALID:
                System.out.println("Processor " + id + " initiates read request for address " + address);
                cacheData[cacheIndex] = mainMemory.readData(address);
                cacheStates[cacheIndex] = CacheLineState.SHARED;
                break;
            case SHARED:
                System.out.println("Processor " + id + " reads data from its cache: " + cacheData[cacheIndex]);
                break;
            case EXCLUSIVE:
                System.out.println("Processor " + id + " reads data from its exclusive cache: " + cacheData[cacheIndex]);
                break;
            case MODIFIED:
                System.out.println("Processor " + id + " reads data from its modified cache: " + cacheData[cacheIndex]);
                break;
        }
        return cacheData[cacheIndex];
    }

    public void writeData(int address, String newData) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        switch (cacheStates[cacheIndex]) {
            case INVALID:
                System.out.println("Processor " + id + " initiates write request for address " + address);
                mainMemory.writeData(address, newData);
                cacheData[cacheIndex] = newData;
                cacheStates[cacheIndex] = CacheLineState.EXCLUSIVE;
                break;
            case SHARED:
                System.out.println("Processor " + id + " initiates write request for address " + address);
                mainMemory.writeData(address, newData);
                cacheData[cacheIndex] = newData;
                cacheStates[cacheIndex] = CacheLineState.EXCLUSIVE;
                break;
            case EXCLUSIVE:
                System.out.println("Processor " + id + " writes data to its exclusive cache: " + newData);
                cacheData[cacheIndex] = newData;
                break;
            case MODIFIED:
                System.out.println("Processor " + id + " writes data to its modified cache: " + newData);
                cacheData[cacheIndex] = newData;
                break;
        }
    }

    public void updateCache(int address, String newData, CacheLineState newState) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        System.out.println("Processor " + id + " updates cache in response to write for address " + address + ": " + newData);
        cacheData[cacheIndex] = newData;
        cacheStates[cacheIndex] = newState;
    }
}

public class MultiProcessor {
    static MainMemory mainMemory;
    static Processor processor1;
    static Processor processor2;
    static Processor processor3;
    static Processor processor4;

    public static void main(String[] args) {
        mainMemory = new MainMemory(32);
        processor1 = new Processor(1, mainMemory);
        processor2 = new Processor(2, mainMemory);
        processor3 = new Processor(3, mainMemory);
        processor4 = new Processor(4, mainMemory);

        processor1.readData(0);
        processor2.writeData(0, "New Data from Processor 2");
        processor1.readData(0);
        processor3.readData(2);
        processor4.writeData(2, "New Data from Processor 4");
        processor3.readData(2);
    }
}
