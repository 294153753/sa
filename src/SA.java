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
    private double rate = 0.99; //冷却系数
    private int step = 1;   //状态步数

    private double[] F;    // Fitness (N)

    private final Random rand = new Random();

    private List<double[]> pop; //种群

    private int dim;    //维度
    private int popsize;    //种群数量

    private double width;
    private double height;

    private double[] best;
    private int bestindex = 0;

    private Coordinate[] room = new Coordinate[]{
            new Coordinate(7, 0), new Coordinate(11, 0), new Coordinate(11, 7.5),
            new Coordinate(10, 7.5), new Coordinate(10, 9), new Coordinate(8, 9),
            new Coordinate(8, 5), new Coordinate(0, 5), new Coordinate(0, 3.5),
            new Coordinate(7, 3.5), new Coordinate(7, 0)
    }; // 房间几何信息

    private Coordinate[] wall = new Coordinate[]{
            new Coordinate(7, 0), new Coordinate(7, 3.5), new Coordinate(11, 3.5),
            new Coordinate(11, 0), new Coordinate(7, 0)
    }; // 主墙几何信息

    private Coordinate[][] doors = new Coordinate[][]{
            new Coordinate[]{
                    new Coordinate(0, 3.5), new Coordinate(7, 3.5), new Coordinate(7, 5),
                    new Coordinate(0, 5), new Coordinate(0, 3.5)
            },
            new Coordinate[]{
                    new Coordinate(8, 9), new Coordinate(10, 9), new Coordinate(10, 7.5),
                    new Coordinate(8, 7.5), new Coordinate(8, 9)
            }
    }; // 门区域几何信息

    public SA(int popsize, int dim, List<double[]> pop, double width, double height) {
        this.popsize = popsize;
        this.pop = pop;
        this.dim = dim;
        this.F = new double[popsize];

        this.width = width;
        this.height = height;
    }

    private double getFitness(double[] Xi) {
        // 代理区域1（用餐区域）的四个坐标点
        Coordinate p11 = new Coordinate(Xi[3], Xi[4]);
        Coordinate p12 = new Coordinate(Xi[3] + Xi[0], Xi[4]);
        Coordinate p13 = new Coordinate(Xi[3] + Xi[0], Xi[4] + Xi[1]);
        Coordinate p14 = new Coordinate(Xi[3], Xi[4] + Xi[1]);
        Coordinate[] area1 = new Coordinate[]{
                p11, p12, p13, p14, p11
        };
        // 代理区域2（会客区域）的四个坐标点
        Coordinate p21 = new Coordinate(Xi[8], Xi[9]);
        Coordinate p22 = new Coordinate(Xi[8] + Xi[5], Xi[9]);
        Coordinate p23 = new Coordinate(Xi[8] + Xi[5], Xi[9] + Xi[6]);
        Coordinate p24 = new Coordinate(Xi[8], Xi[9] + Xi[6]);
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
            constraint3 = getC3(doors, new Coordinate[][]{area1, area2});
            constraint4 = getC4(area1);
            constraint5 = getC5(room, area1, area2, Xi);
            constraint7 = getC7(area1, area2);
        }
//        System.out.println(constraint1 + ", " + constraint2 + ", " + constraint4 + ", " + constraint5 + ", " + constraint7);
        return constraint1 * 6000 + constraint2 * 800 + constraint3 * 600 + constraint4 * 1500 + constraint5 * 1000 + constraint6 * 110 + constraint7 * 50;
    }

    private double getC1(Coordinate[] area1, Coordinate[] area2) {
        return (SAutil.getDifference(room, area1) + SAutil.getDifference(room, area2)) / (SAutil.getArea(area1) + SAutil.getArea(area2));
    }

    private double getC2(Coordinate[] area1, Coordinate[] area2) {
        return SAutil.getIntersection(area1, area2) / (SAutil.getArea(area1) + SAutil.getArea(area2));
    }

    private double getC3(Coordinate[][] doors, Coordinate[][] area) {
        double res = 0;
        for(int i = 0; i < doors.length; i++) {
            for(int j = 0; j < area.length; j++) {
                res += SAutil.getIntersection(doors[i], area[j]);
            }
        }
        return res;
    }

    private double getC4(Coordinate[] area) { //修改了主墙约束
        double intersection = SAutil.getIntersection(wall, area);
        double s = SAutil.getArea(area);
        double tmp = intersection / s;
        double res;
        if(tmp >= 0.85) {
            res = 0;
        } else if(tmp > 0.5 && tmp < 0.85) {
            res = 1 - Math.pow(tmp, 2);
        } else {
            res = 1 - Math.pow(tmp, 3);
        }
        return res;
    }

    private double getC5(Coordinate[] room, Coordinate[] area1, Coordinate[] area2, double[] Xi) {
        double tmp1 = SAutil.getDistance(room, area1, Xi[2]);
        if (tmp1 >= 0 && tmp1 <= 0.1) tmp1 = 0;
        else tmp1 = 1 - Math.pow((0.1 / tmp1), 2);
        double tmp2 = SAutil.getDistance(room, area2, Xi[7]);
        if (tmp2 >= 0 && tmp2 <= 0.1) tmp2 = 0;
        else tmp2 = 1 - Math.pow((0.1 / tmp2), 2);
        return tmp1 + tmp2;
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

    private List<double[]> deepClone(List<double[]> list) {
        List<double[]> res = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            res.add(list.get(i).clone());
        }
        return res;
    }

    public double[] runSA() {
        while (T > 1e-15) {
//            for (int iter = 0; iter < 1000; iter++) { //迭代次数
                List<double[]> newpop = deepClone(pop);
                int index = rand.nextInt(dim);
                double stepindex = (-step) + rand.nextDouble() * (step - (-step)) % (step - (-step) + 1);
                int tmp = (int) Math.abs(Math.floor(stepindex)) % 4;
                for (int i = 0; i < popsize; i++) {
                    if (index == 2 || index == 7) { // 角度
                        newpop.get(i)[index] = (newpop.get(i)[index] + tmp) % 4;
                    } else if (index == 0 || index == 5 || index == 1 || index == 6) { //长宽
                        if (newpop.get(i)[index] + stepindex > 1 && newpop.get(i)[index] + stepindex < 4) {
                            newpop.get(i)[index] += stepindex;
                        } else if(newpop.get(i)[index] + stepindex < 1) {
                            newpop.get(i)[index] *= 2;
                        } else {
                            newpop.get(i)[index] /= 2;
                        }
                    } else if(index == 2 || index == 7) { // x坐标
                        if (newpop.get(i)[index] + stepindex > 0 && newpop.get(i)[index] + stepindex < width) {
                            newpop.get(i)[index] += stepindex;
                        } else if(newpop.get(i)[index] + stepindex < 0) {
                            newpop.get(i)[index] *= 2;
                        } else {
                            newpop.get(i)[index] /= 2;
                        }
                    } else if(index == 3 || index == 8) { // y坐标
                        if (newpop.get(i)[index] + stepindex > 1 && newpop.get(i)[index] + stepindex < height) {
                            newpop.get(i)[index] += stepindex;
                        } else if(newpop.get(i)[index] + stepindex < 1) {
                            newpop.get(i)[index] *= 2;
                        } else {
                            newpop.get(i)[index] /= 2;
                        }
                    }

//                    newpop.get(i)[index] += stepindex;

                    double fold = getFitness(pop.get(i));//老解
                    double fnew = getFitness(newpop.get(i));//新解
                    System.out.println("第" + i + "组：" + fold + ", " + fnew + ", " + Math.exp(-(fnew - fold) / T));
                    if (fnew < fold || Math.random() < Math.exp(-(fnew - fold) / T)) {
                        for (int j = 0; j < popsize; j++) {
                            for (int k = 0; k < dim; k++) {
                                pop.get(i)[k] = newpop.get(i)[k];
                            }
                        }
                    }
                }
                T = T * rate;
//            }
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
        for (int i = 0; i < 8; i++) { //随机解个数
            r[0] = nextDouble(0, 11);
            r[1] = nextDouble(0, 11);
            r[2] = nextInt(0, 4); // 0:向下 1:向左 2:向上 3:向右
            r[3] = Math.abs(r[0]);
            r[4] = Math.abs(r[1]);
            r[5] = nextDouble(0, 11);
            r[6] = nextDouble(0, 11);
            r[7] = nextInt(0, 4); // 0:向下 1:向左 2:向上 3:向右
            r[8] = Math.abs(r[4]);
            r[9] = Math.abs(r[5]);
            pop.add(r);
            r = new double[10];
        }

//        System.out.println(pop);

        SA sa = new SA(pop.size(), 10, pop, 11, 9);
        long now = System.currentTimeMillis();
        double[] res = sa.runSA();
        System.out.println("耗时：" + (System.currentTimeMillis() - now) / 1000 + "秒");

        Coordinate[] room = new Coordinate[]{
                new Coordinate(7, 0), new Coordinate(11, 0), new Coordinate(11, 7.5),
                new Coordinate(10, 7.5), new Coordinate(10, 9), new Coordinate(8, 9),
                new Coordinate(8, 5), new Coordinate(0, 5), new Coordinate(0, 3.5),
                new Coordinate(7, 3.5), new Coordinate(7, 0)
        }; // 房间几何信息
        // 代理区域1（用餐区域）的四个坐标点
        Coordinate p11 = new Coordinate(res[3], res[4]);
        Coordinate p12 = new Coordinate(res[3] + res[0], res[4]);
        Coordinate p13 = new Coordinate(res[3] + res[0], res[4] + res[1]);
        Coordinate p14 = new Coordinate(res[3], res[4] + res[1]);
        Coordinate[] area1 = new Coordinate[]{
                p11, p12, p13, p14, p11
        };
        // 代理区域2（会客区域）的四个坐标点
        Coordinate p21 = new Coordinate(res[8], res[9]);
        Coordinate p22 = new Coordinate(res[8] + res[5], res[9]);
        Coordinate p23 = new Coordinate(res[8] + res[5], res[9] + res[6]);
        Coordinate p24 = new Coordinate(res[8], res[9] + res[6]);
        Coordinate[] area2 = new Coordinate[]{
                p21, p22, p23, p24, p21
        };
        Display.draw(room, area1, area2);

        System.out.println(Arrays.toString(res));
    }
}
