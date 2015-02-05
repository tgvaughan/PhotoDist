/*
 * Copyright (C) 2015 Tim Vaughan (tgvaughan@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package photodist;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tim Vaughan (tgvaughan@gmail.com)
 */
public class Geometry {

    List<GeometryListener> listeners;

    public static class PointPair {
        int[] x, y;

        public PointPair() {
            x = new int[2];
            y = new int[2];
        }
    }

    List<List<PointPair>> paths;
    double fovH, fovV, sep;

    List<PointPair> currentPath;

    public Geometry() {
        paths = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public void addListener(GeometryListener gl) {
        listeners.add(gl);
    }

    public void notifyListeners() {
        for (GeometryListener listener : listeners)
            listener.geometryChanged();
    }

    public void addPath(List<PointPair> path) {
        paths.add(path);
        notifyListeners();
    }

    public List<List<PointPair>> getPaths() {
        return paths;
    }

    public List<PointPair> getCurrentPath() {
        return currentPath;
    }

    public void endPath() {
        currentPath = null;
        notifyListeners();
    }

    public void addPoint(PointPair point) {
        if (currentPath == null) {
            currentPath = new ArrayList<>();
            paths.add(currentPath);
        }
        currentPath.add(point);
        notifyListeners();
    }

    public void setSeparation(double separation) {
        sep = separation;
    }

    public double getSeparation() {
        return sep;
    }

    public void setHorizontalFOV(double fov) {
        fovH = fov;
    }

    public void setVerticalFOV(double fov) {
        fovV = fov;
    }

}
