package com.tryright;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * TextPointStore - reads points from text-encoded files
 * Format: First line contains count, followed by "x y" pairs
 */
public class TextPointStore implements PointStore {
    
    private final int[] xCoords;
    private final int[] yCoords;
    private final int numPoints;
    
    /**
     * Constructor - reads and parses text file
     * @param filename path to text-encoded file
     */
    public TextPointStore(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            
            if (line == null) {
                throw new IOException("Empty file");
            }
            
            int expectedCount;
            try {
                expectedCount = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                throw new IOException("First line must be an integer");
            }

            if (expectedCount < 0) {
                throw new IOException("First line must be a non-negative integer");
            }

            this.numPoints = expectedCount;
            this.xCoords = new int[expectedCount];
            this.yCoords = new int[expectedCount];
            
            int pointCount = 0;
            while (pointCount < expectedCount) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length != 2) {
                    throw new IOException("Invalid point format: expected 'x y'");
                }

                try {
                    xCoords[pointCount] = Integer.parseInt(parts[0]);
                    yCoords[pointCount] = Integer.parseInt(parts[1]);
                    pointCount++;
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid coordinate values");
                }
            }
            
            if (pointCount < expectedCount) {
                throw new IOException("Expected " + expectedCount + " points but found only " + pointCount);
            }
        }
    }
    
    @Override
    public int getX(int idx) {
        if (idx < 0 || idx >= numPoints) {
            throw new IndexOutOfBoundsException("Index " + idx + " out of bounds for " + numPoints + " points");
        }
        return xCoords[idx];
    }
    
    @Override
    public int getY(int idx) {
        if (idx < 0 || idx >= numPoints) {
            throw new IndexOutOfBoundsException("Index " + idx + " out of bounds for " + numPoints + " points");
        }
        return yCoords[idx];
    }
    
    @Override
    public int numPoints() {
        return numPoints;
    }
    
    @Override
    public void close() {
        // No resources to close for text files (BufferedReader closed in constructor)
    }
}
