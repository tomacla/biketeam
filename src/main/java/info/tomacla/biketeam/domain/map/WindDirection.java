package info.tomacla.biketeam.domain.map;

import info.tomacla.biketeam.common.geo.Vector;
import org.springframework.data.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public enum WindDirection {

    NORTH(0),
    NORTH_EAST(45),
    EAST(90),
    SOUTH_EAST(135),
    SOUTH(180),
    SOUTH_WEST(225),
    WEST(270),
    NORTH_WEST(315);

    final int angle;

    WindDirection(int angle) {
        this.angle = angle;
    }

    public static WindDirection findDirectionFromVector(Vector windVector) {

        return Stream.of(WindDirection.values())
                .map(wd -> {
                    double angle = Math.toRadians(90 - wd.angle);
                    double x1 = Math.cos(angle);
                    double y1 = Math.sin(angle) * -1;
                    double x2 = windVector.getX();
                    double y2 = windVector.getY();
                    double dotProduct = (x1 * x2) + (y1 * y2);
                    return Pair.of(wd, 0.5 + (0.5 * dotProduct));
                })
                .sorted(Comparator.comparing(Pair::getSecond))
                .map(Pair::getFirst)
                .findFirst()
                .orElse(WindDirection.NORTH);


    }

}
