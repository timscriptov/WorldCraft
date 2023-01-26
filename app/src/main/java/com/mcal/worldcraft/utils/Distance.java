package com.mcal.worldcraft.utils;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.FloatMath;
import com.mcal.droid.rugl.util.geom.Vector3f;
import com.mcal.droid.rugl.util.geom.Vector3i;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Distance {
    static final double FUZZ = 1.0E-5d;

    private static final double DIST(double in) {
        return Math.abs(in);
    }

    public static double rayToSegment(Point3d rayorig, Vector3d raydir, Point3d segstart, Point3d segend) {
        return rayToSegment(rayorig, raydir, segstart, segend, null, null, null);
    }

    public static double rayToSegment(Point3d rayorig, @NonNull Vector3d raydir, Point3d segstart, Point3d segend, Point3d rayint, Point3d segint, double[] param) {
        Vector3d diff = new Vector3d();
        diff.sub(rayorig, segstart);
        Vector3d segdir = new Vector3d();
        segdir.sub(segend, segstart);
        double A = raydir.dot(raydir);
        double B = -raydir.dot(segdir);
        double C = segdir.dot(segdir);
        double D = raydir.dot(diff);
        double F = diff.dot(diff);
        double det = Math.abs((A * C) - (B * B));
        if (det >= FUZZ) {
            double E = -segdir.dot(diff);
            double s = (B * E) - (C * D);
            double t = (B * D) - (A * E);
            if (s >= 0.0d) {
                if (t >= 0.0d) {
                    if (t <= det) {
                        double invDet = 1.0d / det;
                        double s2 = s * invDet;
                        double t2 = t * invDet;
                        if (rayint != null) {
                            rayint.scaleAdd(s2, raydir, rayorig);
                        }
                        if (segint != null) {
                            segint.scaleAdd(t2, segdir, segstart);
                        }
                        if (param != null) {
                            param[0] = s2;
                            param[1] = t2;
                        }
                        return DIST((((A * s2) + (B * t2) + (2.0d * D)) * s2) + (((B * s2) + (C * t2) + (2.0d * E)) * t2) + F);
                    } else if (D >= 0.0d) {
                        if (rayint != null) {
                            rayint.set(rayorig);
                        }
                        if (segint != null) {
                            segint.set(segend);
                        }
                        if (param != null) {
                            param[0] = 0.0d;
                            param[1] = 1.0d;
                        }
                        return DIST((2.0d * E) + C + F);
                    } else {
                        double s3 = (-D) / A;
                        if (rayint != null) {
                            rayint.scaleAdd(s3, raydir, rayorig);
                        }
                        if (segint != null) {
                            segint.set(segend);
                        }
                        if (param != null) {
                            param[0] = s3;
                            param[1] = 1.0d;
                        }
                        return DIST((((2.0d * B) + D) * s3) + C + (2.0d * E) + F);
                    }
                } else if (D >= 0.0d) {
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.set(segstart);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 0.0d;
                    }
                    return DIST(F);
                } else {
                    double s4 = (-D) / A;
                    if (rayint != null) {
                        rayint.scaleAdd(s4, raydir, rayorig);
                    }
                    if (segint != null) {
                        segint.set(segstart);
                    }
                    if (param != null) {
                        param[0] = s4;
                        param[1] = 0.0d;
                    }
                    return DIST((D * s4) + F);
                }
            } else if (t <= 0.0d) {
                if (D < 0.0d) {
                    double s5 = (-D) / A;
                    if (rayint != null) {
                        rayint.scaleAdd(s5, raydir, rayorig);
                    }
                    if (segint != null) {
                        segint.set(segstart);
                    }
                    if (param != null) {
                        param[0] = s5;
                        param[1] = 0.0d;
                    }
                    return DIST((D * s5) + F);
                } else if (E >= 0.0d) {
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.set(segstart);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 0.0d;
                    }
                    return DIST(F);
                } else if ((-E) >= C) {
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.set(segend);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 1.0d;
                    }
                    return DIST((2.0d * E) + C + F);
                } else {
                    double t3 = (-E) / C;
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.scaleAdd(t3, segdir, segstart);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = t3;
                    }
                    return DIST((E * t3) + F);
                }
            } else if (t <= det) {
                if (E >= 0.0d) {
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.set(segstart);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 0.0d;
                    }
                    return DIST(F);
                } else if ((-E) >= C) {
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.set(segend);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 1.0d;
                    }
                    return DIST((2.0d * E) + C + F);
                } else {
                    double t4 = (-E) / C;
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.scaleAdd(t4, segdir, segstart);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = t4;
                    }
                    return DIST((E * t4) + F);
                }
            } else {
                double tmp = B + D;
                if (tmp < 0.0d) {
                    double s6 = (-tmp) / A;
                    if (rayint != null) {
                        rayint.scaleAdd(s6, raydir, rayorig);
                    }
                    if (segint != null) {
                        segint.set(segend);
                    }
                    if (param != null) {
                        param[0] = s6;
                        param[1] = 1.0d;
                    }
                    return DIST((tmp * s6) + C + (2.0d * E) + F);
                } else if (E >= 0.0d) {
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.set(segstart);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 0.0d;
                    }
                    return DIST(F);
                } else if ((-E) >= C) {
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.set(segend);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 1.0d;
                    }
                    return DIST((2.0d * E) + C + F);
                } else {
                    double t5 = (-E) / C;
                    if (rayint != null) {
                        rayint.set(rayorig);
                    }
                    if (segint != null) {
                        segint.scaleAdd(t5, segdir, segstart);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = t5;
                    }
                    return DIST((E * t5) + F);
                }
            }
        } else if (B > 0.0d) {
            if (D >= 0.0d) {
                if (rayint != null) {
                    rayint.set(rayorig);
                }
                if (segint != null) {
                    segint.set(segstart);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = 0.0d;
                }
                return DIST(F);
            }
            double s7 = (-D) / A;
            if (rayint != null) {
                rayint.scaleAdd(s7, raydir, rayorig);
            }
            if (segint != null) {
                segint.set(segstart);
            }
            if (param != null) {
                param[0] = s7;
                param[1] = 0.0d;
            }
            return DIST((D * s7) + F);
        } else {
            double E2 = segdir.dot(diff);
            double tmp2 = B + D;
            if (tmp2 >= 0.0d) {
                if (rayint != null) {
                    rayint.set(rayorig);
                }
                if (segint != null) {
                    segint.set(segend);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = 1.0d;
                }
                return DIST((2.0d * E2) + C + F);
            }
            double s8 = (-tmp2) / A;
            if (rayint != null) {
                rayint.scaleAdd(s8, raydir, rayorig);
            }
            if (segint != null) {
                segint.set(segend);
            }
            if (param != null) {
                param[0] = s8;
                param[1] = 1.0d;
            }
            return DIST((tmp2 * s8) + C + (2.0d * E2) + F);
        }
    }

    public static double rayToRay(Point3d ray0orig, Vector3d ray0dir, Point3d ray1orig, Vector3d ray1dir) {
        return rayToRay(ray0orig, ray0dir, ray1orig, ray1dir, null, null, null);
    }

    public static double rayToRay(Point3d ray0orig, @NonNull Vector3d ray0dir, Point3d ray1orig, Vector3d ray1dir, Point3d ray0int, Point3d ray1int, double[] param) {
        Vector3d diff = new Vector3d();
        diff.sub(ray0orig, ray1orig);
        double A = ray0dir.dot(ray0dir);
        double B = -ray0dir.dot(ray1dir);
        double C = ray1dir.dot(ray1dir);
        double D = ray0dir.dot(diff);
        double F = diff.dot(diff);
        double det = Math.abs((A * C) - (B * B));
        if (det >= FUZZ) {
            double E = -ray1dir.dot(diff);
            double s = (B * E) - (C * D);
            double t = (B * D) - (A * E);
            if (s >= 0.0d) {
                if (t >= 0.0d) {
                    double invDet = 1.0d / det;
                    double s2 = s * invDet;
                    double t2 = t * invDet;
                    if (ray0int != null) {
                        ray0int.scaleAdd(s2, ray0dir, ray0orig);
                    }
                    if (ray1int != null) {
                        ray1int.scaleAdd(t2, ray1dir, ray1orig);
                    }
                    if (param != null) {
                        param[0] = s2;
                        param[1] = t2;
                    }
                    return DIST((((A * s2) + (B * t2) + (2.0d * D)) * s2) + (((B * s2) + (C * t2) + (2.0d * E)) * t2) + F);
                } else if (D >= 0.0d) {
                    if (ray0int != null) {
                        ray0int.set(ray0orig);
                    }
                    if (ray1int != null) {
                        ray1int.set(ray1orig);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 0.0d;
                    }
                    return DIST(F);
                } else {
                    double s3 = (-D) / A;
                    if (ray0int != null) {
                        ray0int.scaleAdd(s3, ray0dir, ray0orig);
                    }
                    if (ray1int != null) {
                        ray1int.set(ray1orig);
                    }
                    if (param != null) {
                        param[0] = s3;
                        param[1] = 0.0d;
                    }
                    return DIST((D * s3) + F);
                }
            } else if (t >= 0.0d) {
                if (E >= 0.0d) {
                    if (ray0int != null) {
                        ray0int.set(ray0orig);
                    }
                    if (ray1int != null) {
                        ray1int.set(ray1orig);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 0.0d;
                    }
                    return DIST(F);
                }
                double t3 = (-E) / C;
                if (ray0int != null) {
                    ray0int.set(ray0orig);
                }
                if (ray1int != null) {
                    ray1int.scaleAdd(t3, ray1dir, ray1orig);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = t3;
                }
                return DIST((E * t3) + F);
            } else if (D < 0.0d) {
                double s4 = (-D) / A;
                if (ray0int != null) {
                    ray0int.scaleAdd(s4, ray0dir, ray0orig);
                }
                if (ray1int != null) {
                    ray1int.set(ray1orig);
                }
                if (param != null) {
                    param[0] = s4;
                    param[1] = 0.0d;
                }
                return DIST((D * s4) + F);
            } else if (E >= 0.0d) {
                if (ray0int != null) {
                    ray0int.set(ray0orig);
                }
                if (ray1int != null) {
                    ray1int.set(ray1orig);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = 0.0d;
                }
                return DIST(F);
            } else {
                double t4 = (-E) / C;
                if (ray0int != null) {
                    ray0int.set(ray0orig);
                }
                if (ray1int != null) {
                    ray1int.scaleAdd(t4, ray1dir, ray1orig);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = t4;
                }
                return DIST((E * t4) + F);
            }
        } else if (B > 0.0d) {
            if (D >= 0.0d) {
                if (ray0int != null) {
                    ray0int.set(ray0orig);
                }
                if (ray1int != null) {
                    ray1int.set(ray1orig);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = 0.0d;
                }
                return DIST(F);
            }
            double s5 = (-D) / A;
            if (ray0int != null) {
                ray0int.scaleAdd(s5, ray0dir, ray0orig);
            }
            if (ray1int != null) {
                ray1int.set(ray1orig);
            }
            if (param != null) {
                param[0] = s5;
                param[1] = 0.0d;
            }
            return DIST((D * s5) + F);
        } else if (D >= 0.0d) {
            double E2 = ray1dir.dot(diff);
            double t5 = (-E2) / C;
            if (ray0int != null) {
                ray0int.set(ray0orig);
            }
            if (ray1int != null) {
                ray1int.scaleAdd(t5, ray1dir, ray1orig);
            }
            if (param != null) {
                param[0] = 0.0d;
                param[1] = t5;
            }
            return DIST((E2 * t5) + F);
        } else {
            double s6 = (-D) / A;
            if (ray0int != null) {
                ray0int.scaleAdd(s6, ray0dir, ray0orig);
            }
            if (ray1int != null) {
                ray1int.set(ray1orig);
            }
            if (param != null) {
                param[0] = s6;
                param[1] = 0.0d;
            }
            return DIST((D * s6) + F);
        }
    }

    public static double pointToRay(Point3d pt, Point3d rayorig, Vector3d raydir) {
        return pointToRay(pt, rayorig, raydir, null, null);
    }

    public static double pointToRay(Point3d pt, Point3d rayorig, @NonNull Vector3d raydir, Point3d rayint, double[] param) {
        Vector3d diff = new Vector3d();
        diff.sub(pt, rayorig);
        double t = raydir.dot(diff);
        if (t <= 0.0d) {
            if (rayint != null) {
                rayint.set(rayorig);
            }
            if (param != null) {
                param[0] = 0.0d;
            }
        } else {
            double t2 = t / raydir.dot(raydir);
            diff.scaleAdd(-t2, raydir, diff);
            if (rayint != null) {
                rayint.scaleAdd(t2, raydir, rayorig);
            }
            if (param != null) {
                param[0] = t2;
            }
        }
        return diff.dot(diff);
    }

    public static double pointToSegment(Point3d pt, Point3d segstart, Point3d segend) {
        return pointToSegment(pt, segstart, segend, null, null);
    }

    public static double pointToSegment(Point3d pt, Point3d segstart, Point3d segend, Point3d segint, double[] param) {
        Vector3d segdir = new Vector3d();
        segdir.sub(segend, segstart);
        Vector3d diff = new Vector3d();
        diff.sub(pt, segstart);
        double t = segdir.dot(diff);
        if (t <= 0.0d) {
            if (segint != null) {
                segint.set(segstart);
            }
            if (param != null) {
                param[0] = 0.0d;
            }
        } else {
            double mDotm = segdir.dot(segdir);
            if (t >= mDotm) {
                diff.sub(segdir);
                if (segint != null) {
                    segint.set(segend);
                }
                if (param != null) {
                    param[0] = 1.0d;
                }
            } else {
                double t2 = t / mDotm;
                diff.scaleAdd(-t2, segdir, diff);
                if (segint != null) {
                    segint.scaleAdd(t2, segdir, segstart);
                }
                if (param != null) {
                    param[0] = t2;
                }
            }
        }
        return diff.dot(diff);
    }

    public static double segmentToSegment(Point3d seg0start, Point3d seg0end, Point3d seg1start, Point3d seg1end) {
        return segmentToSegment(seg0start, seg0end, seg1start, seg1end, null, null, null);
    }

    public static double segmentToSegment(Point3d seg0start, Point3d seg0end, Point3d seg1start, Point3d seg1end, Point3d seg0int, Point3d seg1int, double[] param) {
        Vector3d diff = new Vector3d();
        diff.sub(seg0start, seg1start);
        Vector3d seg0dir = new Vector3d();
        seg0dir.sub(seg0end, seg0start);
        Vector3d seg1dir = new Vector3d();
        seg1dir.sub(seg1end, seg1start);
        double A = seg0dir.dot(seg0dir);
        double B = -seg0dir.dot(seg1dir);
        double C = seg1dir.dot(seg1dir);
        double D = seg0dir.dot(diff);
        double F = diff.dot(diff);
        double det = Math.abs((A * C) - (B * B));
        if (det >= FUZZ) {
            double E = -seg1dir.dot(diff);
            double s = (B * E) - (C * D);
            double t = (B * D) - (A * E);
            if (s >= 0.0d) {
                if (s <= det) {
                    if (t >= 0.0d) {
                        if (t <= det) {
                            double invDet = 1.0d / det;
                            double s2 = s * invDet;
                            double t2 = t * invDet;
                            if (seg0int != null) {
                                seg0int.scaleAdd(s2, seg0dir, seg0start);
                            }
                            if (seg1int != null) {
                                seg1int.scaleAdd(t2, seg1dir, seg1start);
                            }
                            if (param != null) {
                                param[0] = s2;
                                param[1] = t2;
                            }
                            return DIST((((A * s2) + (B * t2) + (2.0d * D)) * s2) + (((B * s2) + (C * t2) + (2.0d * E)) * t2) + F);
                        }
                        double tmp = B + D;
                        if (tmp >= 0.0d) {
                            if (seg0int != null) {
                                seg0int.set(seg0start);
                            }
                            if (seg1int != null) {
                                seg1int.set(seg1end);
                            }
                            if (param != null) {
                                param[0] = 0.0d;
                                param[1] = 1.0d;
                            }
                            return DIST((2.0d * E) + C + F);
                        } else if ((-tmp) >= A) {
                            if (seg0int != null) {
                                seg0int.set(seg0end);
                            }
                            if (seg1int != null) {
                                seg1int.set(seg1end);
                            }
                            if (param != null) {
                                param[0] = 1.0d;
                                param[1] = 1.0d;
                            }
                            return DIST(A + C + F + (2.0d * (E + tmp)));
                        } else {
                            double s3 = (-tmp) / A;
                            if (seg0int != null) {
                                seg0int.scaleAdd(s3, seg0dir, seg0start);
                            }
                            if (seg1int != null) {
                                seg1int.set(seg1end);
                            }
                            if (param != null) {
                                param[0] = s3;
                                param[1] = 1.0d;
                            }
                            return DIST((tmp * s3) + C + (2.0d * E) + F);
                        }
                    } else if (D >= 0.0d) {
                        if (seg0int != null) {
                            seg0int.set(seg0start);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1start);
                        }
                        if (param != null) {
                            param[0] = 0.0d;
                            param[1] = 0.0d;
                        }
                        return DIST(F);
                    } else if ((-D) >= A) {
                        if (seg0int != null) {
                            seg0int.set(seg0end);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1start);
                        }
                        if (param != null) {
                            param[0] = 1.0d;
                            param[1] = 0.0d;
                        }
                        return DIST((2.0d * D) + A + F);
                    } else {
                        double s4 = (-D) / A;
                        if (seg0int != null) {
                            seg0int.scaleAdd(s4, seg0dir, seg0start);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1start);
                        }
                        if (param != null) {
                            param[0] = s4;
                            param[1] = 0.0d;
                        }
                        return DIST((D * s4) + F);
                    }
                } else if (t >= 0.0d) {
                    if (t <= det) {
                        double tmp2 = B + E;
                        if (tmp2 >= 0.0d) {
                            if (seg0int != null) {
                                seg0int.set(seg0end);
                            }
                            if (seg1int != null) {
                                seg1int.set(seg1start);
                            }
                            if (param != null) {
                                param[0] = 1.0d;
                                param[1] = 0.0d;
                            }
                            return DIST((2.0d * D) + A + F);
                        } else if ((-tmp2) >= C) {
                            if (seg0int != null) {
                                seg0int.set(seg0end);
                            }
                            if (seg1int != null) {
                                seg1int.set(seg1end);
                            }
                            if (param != null) {
                                param[0] = 1.0d;
                                param[1] = 1.0d;
                            }
                            return DIST(A + C + F + (2.0d * (D + tmp2)));
                        } else {
                            double t3 = (-tmp2) / C;
                            if (seg0int != null) {
                                seg0int.set(seg0end);
                            }
                            if (seg1int != null) {
                                seg1int.scaleAdd(t3, seg1dir, seg1start);
                            }
                            if (param != null) {
                                param[0] = 1.0d;
                                param[1] = t3;
                            }
                            return DIST((tmp2 * t3) + A + (2.0d * D) + F);
                        }
                    }
                    double tmp3 = B + D;
                    if ((-tmp3) <= A) {
                        if (tmp3 >= 0.0d) {
                            if (seg0int != null) {
                                seg0int.set(seg0start);
                            }
                            if (seg1int != null) {
                                seg1int.set(seg1end);
                            }
                            if (param != null) {
                                param[0] = 0.0d;
                                param[1] = 1.0d;
                            }
                            return DIST((2.0d * E) + C + F);
                        }
                        double s5 = (-tmp3) / A;
                        if (seg0int != null) {
                            seg0int.scaleAdd(s5, seg0dir, seg0start);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1end);
                        }
                        if (param != null) {
                            param[0] = s5;
                            param[1] = 1.0d;
                        }
                        return DIST((tmp3 * s5) + C + (2.0d * E) + F);
                    }
                    double tmp4 = B + E;
                    if (tmp4 >= 0.0d) {
                        if (seg0int != null) {
                            seg0int.set(seg0end);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1start);
                        }
                        if (param != null) {
                            param[0] = 1.0d;
                            param[1] = 0.0d;
                        }
                        return DIST((2.0d * D) + A + F);
                    } else if ((-tmp4) >= C) {
                        if (seg0int != null) {
                            seg0int.set(seg0end);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1end);
                        }
                        if (param != null) {
                            param[0] = 1.0d;
                            param[1] = 1.0d;
                        }
                        return DIST(A + C + F + (2.0d * (D + tmp4)));
                    } else {
                        double t4 = (-tmp4) / C;
                        if (seg0int != null) {
                            seg0int.set(seg0end);
                        }
                        if (seg1int != null) {
                            seg1int.scaleAdd(t4, seg1dir, seg1start);
                        }
                        if (param != null) {
                            param[0] = 1.0d;
                            param[1] = t4;
                        }
                        return DIST((tmp4 * t4) + A + (2.0d * D) + F);
                    }
                } else if ((-D) < A) {
                    if (D >= 0.0d) {
                        if (seg0int != null) {
                            seg0int.set(seg0start);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1start);
                        }
                        if (param != null) {
                            param[0] = 0.0d;
                            param[1] = 0.0d;
                        }
                        return DIST(F);
                    }
                    double s6 = (-D) / A;
                    if (seg0int != null) {
                        seg0int.scaleAdd(s6, seg0dir, seg0start);
                    }
                    if (seg1int != null) {
                        seg1int.set(seg1start);
                    }
                    if (param != null) {
                        param[0] = s6;
                        param[1] = 0.0d;
                    }
                    return DIST((D * s6) + F);
                } else {
                    double tmp5 = B + E;
                    if (tmp5 >= 0.0d) {
                        if (seg0int != null) {
                            seg0int.set(seg0end);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1start);
                        }
                        if (param != null) {
                            param[0] = 1.0d;
                            param[1] = 0.0d;
                        }
                        return DIST((2.0d * D) + A + F);
                    } else if ((-tmp5) >= C) {
                        if (seg0int != null) {
                            seg0int.set(seg0end);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1end);
                        }
                        if (param != null) {
                            param[0] = 1.0d;
                            param[1] = 1.0d;
                        }
                        return DIST(A + C + F + (2.0d * (D + tmp5)));
                    } else {
                        double t5 = (-tmp5) / C;
                        if (seg0int != null) {
                            seg0int.set(seg0end);
                        }
                        if (seg1int != null) {
                            seg1int.scaleAdd(t5, seg1dir, seg1start);
                        }
                        if (param != null) {
                            param[0] = 1.0d;
                            param[1] = t5;
                        }
                        return DIST((tmp5 * t5) + A + (2.0d * D) + F);
                    }
                }
            } else if (t >= 0.0d) {
                if (t <= det) {
                    if (E >= 0.0d) {
                        if (seg0int != null) {
                            seg0int.set(seg0start);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1start);
                        }
                        if (param != null) {
                            param[0] = 0.0d;
                            param[1] = 0.0d;
                        }
                        return DIST(F);
                    } else if ((-E) >= C) {
                        if (seg0int != null) {
                            seg0int.set(seg0start);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1end);
                        }
                        if (param != null) {
                            param[0] = 0.0d;
                            param[1] = 1.0d;
                        }
                        return DIST((2.0d * E) + C + F);
                    } else {
                        double t6 = (-E) / C;
                        if (seg0int != null) {
                            seg0int.set(seg0start);
                        }
                        if (seg1int != null) {
                            seg1int.scaleAdd(t6, seg1dir, seg1start);
                        }
                        if (param != null) {
                            param[0] = 0.0d;
                            param[1] = t6;
                        }
                        return DIST((E * t6) + F);
                    }
                }
                double tmp6 = B + D;
                if (tmp6 < 0.0d) {
                    if ((-tmp6) >= A) {
                        if (seg0int != null) {
                            seg0int.set(seg0end);
                        }
                        if (seg1int != null) {
                            seg1int.set(seg1end);
                        }
                        if (param != null) {
                            param[0] = 1.0d;
                            param[1] = 1.0d;
                        }
                        return DIST(A + C + F + (2.0d * (E + tmp6)));
                    }
                    double s7 = (-tmp6) / A;
                    if (seg0int != null) {
                        seg0int.scaleAdd(s7, seg0dir, seg0start);
                    }
                    if (seg1int != null) {
                        seg1int.set(seg1end);
                    }
                    if (param != null) {
                        param[0] = s7;
                        param[1] = 1.0d;
                    }
                    return DIST((tmp6 * s7) + C + (2.0d * E) + F);
                } else if (E >= 0.0d) {
                    if (seg0int != null) {
                        seg0int.set(seg0start);
                    }
                    if (seg1int != null) {
                        seg1int.set(seg1start);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 0.0d;
                    }
                    return DIST(F);
                } else if ((-E) >= C) {
                    if (seg0int != null) {
                        seg0int.set(seg0start);
                    }
                    if (seg1int != null) {
                        seg1int.set(seg1end);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = 1.0d;
                    }
                    return DIST((2.0d * E) + C + F);
                } else {
                    double t7 = (-E) / C;
                    if (seg0int != null) {
                        seg0int.set(seg0start);
                    }
                    if (seg1int != null) {
                        seg1int.scaleAdd(t7, seg1dir, seg1start);
                    }
                    if (param != null) {
                        param[0] = 0.0d;
                        param[1] = t7;
                    }
                    return DIST((E * t7) + F);
                }
            } else if (D < 0.0d) {
                if ((-D) >= A) {
                    if (seg0int != null) {
                        seg0int.set(seg0end);
                    }
                    if (seg1int != null) {
                        seg1int.set(seg1start);
                    }
                    if (param != null) {
                        param[0] = 1.0d;
                        param[1] = 0.0d;
                    }
                    return DIST((2.0d * D) + A + F);
                }
                double s8 = (-D) / A;
                if (seg0int != null) {
                    seg0int.scaleAdd(s8, seg0dir, seg0start);
                }
                if (seg1int != null) {
                    seg1int.set(seg1start);
                }
                if (param != null) {
                    param[0] = s8;
                    param[1] = 0.0d;
                }
                return DIST((D * s8) + F);
            } else if (E >= 0.0d) {
                if (seg0int != null) {
                    seg0int.set(seg0start);
                }
                if (seg1int != null) {
                    seg1int.set(seg1start);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = 0.0d;
                }
                return DIST(F);
            } else if ((-E) >= C) {
                if (seg0int != null) {
                    seg0int.set(seg0start);
                }
                if (seg1int != null) {
                    seg1int.set(seg1end);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = 1.0d;
                }
                return DIST((2.0d * E) + C + F);
            } else {
                double t8 = (-E) / C;
                if (seg0int != null) {
                    seg0int.set(seg0start);
                }
                if (seg1int != null) {
                    seg1int.scaleAdd(t8, seg1dir, seg1start);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = t8;
                }
                return DIST((E * t8) + F);
            }
        } else if (B > 0.0d) {
            if (D >= 0.0d) {
                if (seg0int != null) {
                    seg0int.set(seg0start);
                }
                if (seg1int != null) {
                    seg1int.set(seg1start);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = 0.0d;
                }
                return DIST(F);
            } else if ((-D) <= A) {
                double s9 = (-D) / A;
                if (seg0int != null) {
                    seg0int.scaleAdd(s9, seg0dir, seg0start);
                }
                if (seg1int != null) {
                    seg1int.set(seg1start);
                }
                if (param != null) {
                    param[0] = s9;
                    param[1] = 0.0d;
                }
                return DIST((D * s9) + F);
            } else {
                double E2 = -seg1dir.dot(diff);
                double tmp7 = A + D;
                if ((-tmp7) >= B) {
                    if (seg0int != null) {
                        seg0int.set(seg0end);
                    }
                    if (seg1int != null) {
                        seg1int.set(seg1end);
                    }
                    if (param != null) {
                        param[0] = 1.0d;
                        param[1] = 1.0d;
                    }
                    return DIST(A + C + F + (2.0d * (B + D + E2)));
                }
                double t9 = (-tmp7) / B;
                if (seg0int != null) {
                    seg0int.set(seg0end);
                }
                if (seg1int != null) {
                    seg1int.scaleAdd(t9, seg1dir, seg1start);
                }
                if (param != null) {
                    param[0] = 1.0d;
                    param[1] = t9;
                }
                return DIST((2.0d * D) + A + F + (((C * t9) + (2.0d * (B + E2))) * t9));
            }
        } else if ((-D) >= A) {
            if (seg0int != null) {
                seg0int.set(seg0end);
            }
            if (seg1int != null) {
                seg1int.set(seg1start);
            }
            if (param != null) {
                param[0] = 1.0d;
                param[1] = 0.0d;
            }
            return DIST((2.0d * D) + A + F);
        } else if (D <= 0.0d) {
            double s10 = (-D) / A;
            if (seg0int != null) {
                seg0int.scaleAdd(s10, seg0dir, seg0start);
            }
            if (seg1int != null) {
                seg1int.set(seg1start);
            }
            if (param != null) {
                param[0] = s10;
                param[1] = 0.0d;
            }
            return DIST((D * s10) + F);
        } else {
            double E3 = -seg1dir.dot(diff);
            if (D >= (-B)) {
                if (seg0int != null) {
                    seg0int.set(seg0start);
                }
                if (seg1int != null) {
                    seg1int.set(seg1end);
                }
                if (param != null) {
                    param[0] = 0.0d;
                    param[1] = 1.0d;
                }
                return DIST((2.0d * E3) + C + F);
            }
            double t10 = (-D) / B;
            if (seg0int != null) {
                seg0int.set(seg0start);
            }
            if (seg1int != null) {
                seg1int.scaleAdd(t10, seg1dir, seg1start);
            }
            if (param != null) {
                param[0] = 0.0d;
                param[1] = t10;
            }
            return DIST((((2.0d * E3) + (C * t10)) * t10) + F);
        }
    }

    public static float getDistanceBetweenPoints(@NonNull Vector3i point1, @NonNull Vector3i point2, float defaultValue) {
        return getDistanceBetweenPoints(new Vector3f(point1.x, point1.y, point1.z), new Vector3f(point2.x, point2.y, point2.z), defaultValue);
    }

    public static float getDistanceBetweenPoints(Vector3f point1, @NonNull Vector3i point2, float defaultValue) {
        return getDistanceBetweenPoints(point1, new Vector3f(point2.x, point2.y, point2.z), defaultValue);
    }

    public static float getDistanceBetweenPoints(@NonNull Vector3i point1, Vector3f point2, float defaultValue) {
        return getDistanceBetweenPoints(new Vector3f(point1.x, point1.y, point1.z), point2, defaultValue);
    }

    public static float getDistanceBetweenPoints(Vector3f point1, Vector3f point2, float defaultValue) {
        try {
            return FloatMath.sqrt((float) (Math.pow(point1.x - point2.x, 2.0d) + Math.pow(point1.y - point2.y, 2.0d) + Math.pow(point1.z - point2.z, 2.0d)));
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }
}
