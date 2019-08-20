import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 模拟退火算法 Created by Ping on 2019/5/19.
 */
public class SA {
    private double T = 10000.0; //温度
    private double rate = 0.75; //冷却系数
    private int step = 1;   //状态步数

    private double[] F;    // Fitness (N)

    private final Random rand = new Random();

    private List<double[]> pop; //种群

    private int dim;    //维度
    private int popsize;    //种群数量

    private double[] best;
    private int bestindex = 0;

    private Coordinate[] room = new Coordinate[]{
            new Coordinate(7, 0), new Coordinate(11, 0), new Coordinate(11, 7.5),
            new Coordinate(10, 7.5), new Coordinate(10, 9), new Coordinate(8, 9),
            new Coordinate(8, 5), new Coordinate(0, 5), new Coordinate(0, 3.5),
            new Coordinate(7, 3.5), new Coordinate(7, 0)
    }; // 房间几何信息

    public SA(int popsize, int dim, List<double[]> pop) {
        this.popsize = popsize;
        this.pop = pop;

        this.dim = dim;

        this.F = new double[popsize];
    }

    private double getFitness(double[] Xi) {
        // 代理区域1（用餐区域）的四个坐标点
        Coordinate p11 = new Coordinate(Xi[2], Xi[3]);
        Coordinate p12 = new Coordinate(Xi[2] + Xi[0], Xi[3]);
        Coordinate p13 = new Coordinate(Xi[2] + Xi[0], Xi[3] + Xi[1]);
        Coordinate p14 = new Coordinate(Xi[2], Xi[3] + Xi[1]);
        Coordinate[] area1 = new Coordinate[]{
                p11, p12, p13, p14, p11
        };
        // 代理区域2（会客区域）的四个坐标点
        Coordinate p21 = new Coordinate(Xi[6], Xi[7]);
        Coordinate p22 = new Coordinate(Xi[6] + Xi[4], Xi[7]);
        Coordinate p23 = new Coordinate(Xi[6] + Xi[4], Xi[7] + Xi[5]);
        Coordinate p24 = new Coordinate(Xi[6], Xi[7] + Xi[5]);
        Coordinate[] area2 = new Coordinate[]{
                p21, p22, p23, p24, p21
        };
        //先用两个代理区域计算
        double constraint1 = 0;
        double constraint2 = 0;
        double constraint3 = 0;
        double constraint4 = 0;
        double constraint5 = 0;
        double constraint6 = 0;
        double constraint7 = 0;
        if (Xi.length == 10) {
            constraint1 = getC1(area1, area2);
            constraint2 = getC2(area1, area2);
            constraint4 = getC4(Xi[4]);
            constraint7 = getC7(area1, area2);
        }

        return constraint1 * 5000 + constraint2 * 600 + constraint3 * 600 + constraint4 * 1500 + constraint5 * 1000 + constraint6 * 110 + constraint7 * 50;
    }

    private double getC1(Coordinate[] area1, Coordinate[] area2) {
        return (SAutil.getDifference(room, area1) + SAutil.getDifference(room, area2)) / (SAutil.getArea(area1) + SAutil.getArea(area2));
    }

    private double getC2(Coordinate[] area1, Coordinate[] area2) {
        return SAutil.getIntersection(area1, area2) / (SAutil.getArea(area1) + SAutil.getArea(area2));
    }

    private double getC4(double angle) {
        double tmp = 0;
        if (angle > 0) {
            tmp = Math.PI / 2 * 3;
        } else if (angle < 0) {
            tmp = Math.PI / 2;
        } else {
            tmp = Double.POSITIVE_INFINITY;
        }
        return Math.abs(tmp - (Math.PI));
    }

    private double getC7(Coordinate[] area1, Coordinate[] area2) {
        double S = (SAutil.getArea(area1) + SAutil.getArea(area2)) / SAutil.getArea(room);
        double res = 0;
        if (S >= 0.4 && S <= 0.55) {
            res = 0;
        } else if (S < 0.4) {
            res = 1 - Math.pow(S / 0.4, 2);
        } else if (S > 0.55) {
            res = 1 - Math.pow(0.55 / S, 2);
        }
        return res;
    }

    public double[] runSA() {
        List<double[]> newpop = new ArrayList<>();
        newpop.addAll(pop);

        while (T > 0.1) {
            for (int iter = 0; iter < 1000; iter++) { //迭代次数
                int index = rand.nextInt(dim);
                double stepindex = (-step) + rand.nextDouble() * (step - (-step)) % (step - (-step) + 1);
                int tmp = (int)Math.abs(Math.floor(stepindex)) % 4;
                for (int i = 0; i < popsize; i++) {
                    if(index == 2 || index == 7) { // 角度
                        newpop.get(i)[index] = (newpop.get(i)[index] + tmp) % 4;
                    } else if(index == 0 || index == 1 || index == 5 || index == 6) { // 长宽
                        newpop.get(i)[index] += stepindex;
                    } else { // 坐标
                        newpop.get(i)[index] += stepindex;
                    }

//                    newpop.get(i)[index] += stepindex;

                    double fold = getFitness(pop.get(i));//老解
                    double fnew = getFitness(newpop.get(i));//新解

                    if (fnew < fold || Math.random() < Math.exp(-(fnew - fold) / T)) {
                        for (int j = 0; j < popsize; j++) {
                            for (int k = 0; k < dim; k++) {
                                pop.get(i)[k] = newpop.get(i)[k];
                            }
                        }
                    }
                }
                T = T * rate;
            }
        }

        F = new double[pop.size()];
        for (int i = 0; i < pop.size(); i++) {
            F[i] = getFitness(pop.get(i));
            if (F[i] < F[bestindex]) {
                bestindex = i;
            }
        }
//        System.out.println("最小适应度为："+ s[bestindex]);
        best = pop.get(bestindex);

        return best;
    }

    public static double nextDouble(final double min, final double max) {
        return min + ((max - min) * new Random().nextDouble());
    }

    public static int nextInt(final int min, final int max) {
        return new Random().nextInt(max) % (max - min + 1) + min;
    }

    public static void main(String[] args) {
        List<double[]> pop = new ArrayList<>();
        double[] r = new double[10];
        for (int i = 0; i < 4; i++) { //随机解个数
            r[0] = nextDouble(0, 11);
            r[1] = nextDouble(0, 11);
            r[2] = nextInt(0, 4);
            r[3] = Math.abs(r[0]);
            r[4] = Math.abs(r[1]);
            r[5] = nextDouble(0, 11);
            r[6] = nextDouble(0, 11);
            r[7] = nextInt(0, 4);
            r[8] = Math.abs(r[4]);
            r[9] = Math.abs(r[5]);
            pop.add(r);
            r = new double[10];
        }

//        System.out.println(pop);

        SA sa = new SA(pop.size(), 10, pop);
        double[] res = sa.runSA();
        System.out.println(Arrays.toString(res));
    }
}
