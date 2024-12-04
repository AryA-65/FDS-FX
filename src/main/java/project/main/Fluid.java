package project.main;

import java.util.Arrays;
import java.util.Objects;

public abstract class Fluid {
    float density;
    int numX, numY, numCells;
    float h;
    boolean[] s;
    float[] u, v, newU, newV, p, m, newM, T, newT;
    float thermalCoef;

    public Fluid(int numX, int numY, float h) {
        this.numX = numX + 2;
        this.numY = numY + 2;
        this.numCells = this.numX * this.numY;
        this.h = h;
        this.u = new float[this.numCells];
        this.v = new float[this.numCells];
        this.newU = new float[this.numCells];
        this.newV = new float[this.numCells];
        this.p = new float[this.numCells];
        this.s = new boolean[this.numCells];
        this.m = new float[this.numCells];
        this.newM = new float[this.numCells];
        this.T = new float[this.numCells];
        this.newT = new float[this.numCells];
        this.thermalCoef = 0.35f;
        Arrays.fill(this.m, 1.0f);
    }

    public abstract void simulate(float dt, float gravity, int numIters);

    public void integrate(float dt, float gravity) {
        int n = this.numY;
        for (int i = 1; i < this.numX; i++) {
            for (int j = 1; j < this.numY - 1; j++) {
                if (this.s[i * n + j] && this.s[i * n + j - 1])
                    this.v[i * n + j] += gravity * dt;
            }
        }
    }

    public void extrapolate() {
        int n = this.numY;
        for (int i = 0; i < this.numX; i++) {
            this.u[i * n] = this.u[i * n + 1];
            this.u[i * n + this.numY - 1] = this.u[i * n + this.numY - 2];
        }
        for (int j = 0; j < this.numY; j++) {
            this.v[j] = this.v[n + j];
            this.v[(this.numX - 1) * n + j] = this.v[(this.numX - 2) * n + j];
        }
    }

    public float sampleField(float x, float y, int field) {
        int n = this.numY;
        float h = this.h;
        float h1 = 1.0f / h;
        float h2 = 0.5f * h;

        x = Math.max(Math.min(x, this.numX * h), h);
        y = Math.max(Math.min(y, this.numY * h), h);

        float dx = 0.0f;
        float dy = 0.0f;

        float[] f;

        switch (field) {
            case Main.U_FIELD:
                f = this.u;
                dy = h2;
                break;
            case Main.V_FIELD:
                f = this.v;
                dx = h2;
                break;
            case Main.S_FIELD:
                f = this.m;
                dx = h2;
                dy = h2;
                break;
            default:
                f = null;
        }

        int x0 = Math.min((int) ((x - dx) * h1), this.numX - 1);
        float tx = ((x - dx) - x0 * h) * h1;
        int x1 = Math.min(x0 + 1, this.numX - 1);

        int y0 = Math.min((int) ((y - dy) * h1), this.numY - 1);
        float ty = ((y - dy) - y0 * h) * h1;
        int y1 = Math.min(y0 + 1, this.numY - 1);

        float sx = 1.0f - tx;
        float sy = 1.0f - ty;

        return sx * sy * Objects.requireNonNull(f)[x0 * n + y0] +
                tx * sy * f[x1 * n + y0] +
                tx * ty * f[x1 * n + y1] +
                sx * ty * f[x0 * n + y1];
    }

    public float avgU(int i, int j) {
        int n = this.numY;
        return (this.u[i * n + j - 1] + this.u[i * n + j] +
                this.u[(i + 1) * n + j - 1] + this.u[(i + 1) * n + j]) * 0.25f;
    }

    public float avgV(int i, int j) {
        int n = this.numY;
        return (this.v[(i - 1) * n + j] + this.v[i * n + j] +
                this.v[(i - 1) * n + j + 1] + this.v[i * n + j + 1]) * 0.25f;
    }

    public void advectVel(float dt) {
        System.arraycopy(this.u, 0, this.newU, 0, this.u.length);
        System.arraycopy(this.v, 0, this.newV, 0, this.v.length);

        int n = this.numY;
        float h = this.h;
        float h2 = 0.5f * h;

        for (int i = 1; i < this.numX; i++) {
            for (int j = 1; j < this.numY; j++) {
                // u component
                if (this.s[i * n + j] && this.s[(i - 1) * n + j] && j < this.numY - 1) {
                    float x = i * h;
                    float y = j * h + h2;
                    float u = this.u[i * n + j];
                    float v = this.avgV(i, j);
                    x = x - dt * u;
                    y = y - dt * v;
                    u = this.sampleField(x, y, Main.U_FIELD);
                    this.newU[i * n + j] = u;
                }
                // v component
                if (this.s[i * n + j] && this.s[i * n + j - 1] && i < this.numX - 1) {
                    float x = i * h + h2;
                    float y = j * h;
                    float u = this.avgU(i, j);
                    float v = this.v[i * n + j];
                    x = x - dt * u;
                    y = y - dt * v;
                    v = this.sampleField(x, y, Main.V_FIELD);
                    this.newV[i * n + j] = v;
                }
            }
        }

        System.arraycopy(this.newU, 0, this.u, 0, this.newU.length);
        System.arraycopy(this.newV, 0, this.v, 0, this.newV.length);
    }

    public void advectSmoke(float dt) {
        System.arraycopy(this.m, 0, this.newM, 0, this.m.length);

        int n = this.numY;
        float h = this.h;
        float h2 = 0.5f * h;

        for (int i = 1; i < this.numX - 1; i++) {
            for (int j = 1; j < this.numY - 1; j++) {
                if (this.s[i * n + j]) {
                    float u = (this.u[i * n + j] + this.u[(i + 1) * n + j]) * 0.5f;
                    float v = (this.v[i * n + j] + this.v[i * n + j + 1]) * 0.5f;
                    float x = i * h + h2 - dt * u;
                    float y = j * h + h2 - dt * v;

                    this.newM[i * n + j] = this.sampleField(x, y, Main.S_FIELD);
                }
            }
        }
        System.arraycopy(this.newM, 0, this.m, 0, this.newM.length);
    }

    public float boolToFloat(boolean b) {
        return b ? 1.0f : 0.0f;
    }
}

class Incompressible extends Fluid {
    public Incompressible(float density, int numX, int numY, float h) {
        super(numX, numY, h);
        this.density = density;
    }

    @Override
    public void simulate(float dt, float gravity, int numIters) {
        this.integrate(dt, gravity);

        Arrays.fill(this.p, 0.0f);
        this.solveIncompressibility(numIters, dt);

        this.extrapolate();
        this.advectVel(dt);
        this.advectSmoke(dt);

//        System.out.println("simming in fluid");
    }

    public void solveIncompressibility(int numIters, float dt) {
        int n = this.numY;
        float cp = this.density * this.h / dt;

        for (int iter = 0; iter < numIters; iter++) {
            for (int i = 1; i < this.numX - 1; i++) {
                for (int j = 1; j < this.numY - 1; j++) {
                    if (!this.s[i * n + j])
                        continue;

                    boolean sx0 = this.s[(i - 1) * n + j];
                    boolean sx1 = this.s[(i + 1) * n + j];
                    boolean sy0 = this.s[i * n + j - 1];
                    boolean sy1 = this.s[i * n + j + 1];
                    float sSum = boolToFloat(sx0) + boolToFloat(sx1) + boolToFloat(sy0) + boolToFloat(sy1);
                    if (sSum == 0.0f)
                        continue;

                    float div = this.u[(i + 1) * n + j] - this.u[i * n + j] +
                            this.v[i * n + j + 1] - this.v[i * n + j];

                    float p = -div / sSum;
                    p *= Engine.overRelaxation;
                    this.p[i * n + j] += cp * p;

                    this.u[i * n + j] -= boolToFloat(sx0) * p;
                    this.u[(i + 1) * n + j] += boolToFloat(sx1) * p;
                    this.v[i * n + j] -= boolToFloat(sy0) * p;
                    this.v[i * n + j + 1] += boolToFloat(sy1) * p;
                }
            }
        }
    }
}

//class Compressible extends Fluid {
//    public Compressible(float[] density, int numX, int numY, float h) {
//        super(numX, numY, h);
//        this.densities = density;
//    }
//
//    @Override
//    public void simulate(float dt, float gravity, int numIters) {
//        this.integrate(dt, gravity);
//
//        Arrays.fill(this.p, 0.0f);
////        this.solveCompressibility(numIters, dt);
//
//        this.extrapolate();
//        this.advectVel(dt);
//        this.advectSmoke(dt);
//    }
//}