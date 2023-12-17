import java.util.HashSet;
import java.util.Set;

// 定义缓存行的状态
enum CacheLineState {
    INVALID, // 失效
    SHARED,  // 共享
    EXCLUSIVE // 独占
}

// 定义主存类
class MainMemory {
    private String[] data;

    public MainMemory(int size) {
        data = new String[size];
        for (int i = 0; i < 32; i++) {
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
    private int id;
    private CacheLineState[] cacheStates; // 每个缓存行的状态
    private String[] cacheData; // 每个缓存行的数据
    private int[] cacheAddress; // 用于存储每个缓存行中的主存地址
    private MainMemory mainMemory;

    public Processor(int id, MainMemory mainMemory) {
        this.id = id;
        this.cacheStates = new CacheLineState[4]; // 每个处理器有4个缓存行
        this.cacheData = new String[4]; // 数据缓存
        this.cacheAddress = new int[4];
        for (int i = 0; i < 4; i++) {
            cacheStates[i] = CacheLineState.INVALID;
            cacheData[i] = "";
            cacheAddress[i]=32;
        }
        this.mainMemory = mainMemory;
    }

    public String readData(int address) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
//        switch (cacheStates[cacheIndex]) {
//            case INVALID:
//                System.out.println("处理器 " + id + " 从主存 " + address +" 读取数据");
//                cacheData[cacheIndex] = mainMemory.readData(address);
//                cacheStates[cacheIndex] = CacheLineState.SHARED;
//                cacheAddress[cacheIndex] = address;
//                break;
//            case SHARED:
//                System.out.println("Processor " + id + " reads data from its cache: " + cacheData[cacheIndex]);
//                break;
//            case EXCLUSIVE:
//                System.out.println("Processor " + id + " reads data from its exclusive cache: " + cacheData[cacheIndex]);
//                break;
//        }
        return cacheData[cacheIndex];
    }

    public void writeData(int address, String newData) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
//        switch (cacheStates[cacheIndex]) {
//            case INVALID:
//                System.out.println("Processor " + id + " initiates write request for address " + address);
//                mainMemory.writeData(address, newData);
//                cacheData[cacheIndex] = newData;
//                cacheStates[cacheIndex] = CacheLineState.EXCLUSIVE;
//                break;
//            case SHARED:
//                System.out.println("Processor " + id + " initiates write request for address " + address);
//                mainMemory.writeData(address, newData);
//                cacheData[cacheIndex] = newData;
//                cacheStates[cacheIndex] = CacheLineState.EXCLUSIVE;
//                break;
//            case EXCLUSIVE:
//                System.out.println("Processor " + id + " writes data to its exclusive cache: " + newData);
//                cacheData[cacheIndex] = newData;
//                break;
//        }
    }

    public void updateCache(int address, String newData, CacheLineState newState) {
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        System.out.println("Processor " + id + " updates cache in response to write for address " + address + ": " + newData);
        cacheData[cacheIndex] = newData;
        cacheStates[cacheIndex] = newState;
    }
    public void checkCache(int address){
        int cacheIndex = address % 4; // 使用取余操作确定缓存行索引
        if(cacheAddress[cacheIndex] == address){
            //todo
        }
    }
}

public class MultiProcessor {
    static MainMemory mainMemory;
    static Processor p1;
    static Processor p2;
    static Processor p3;
    static Processor p4;
    static Set<Processor> processors = new HashSet<>();

    static void checkOtherProcessors(Processor mp,int address){
        for(Processor p : processors){
            if(!mp.equals(p)){
                p.checkCache(address);
            }
        }
    }
    public static void main(String[] args) {
        mainMemory = new MainMemory(32);
        p1 = new Processor(1, mainMemory);
        p2 = new Processor(2, mainMemory);
        p3 = new Processor(3, mainMemory);
        p4 = new Processor(4, mainMemory);
        processors.add(p1);
        processors.add(p2);
        processors.add(p2);
        processors.add(p3);

        p1.readData(0);
        p2.writeData(0, "New Data from Processor 2");
        p1.readData(0);
        p3.readData(2);
        p4.writeData(2, "New Data from Processor 4");
        p3.readData(2);
    }
}
