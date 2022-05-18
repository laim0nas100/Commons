package lt.lb.commons.rows;

import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author laim0nas100
 */
public class DrowUtils {

    public static class ColSpan {

        /**
         * Distribute remaining cell span surplus one by one starting from the
         * right (array end)
         *
         * @param columnSpan
         * @param surplus
         * @return
         */
        public static Integer[] distributeSurplusRight(Integer[] columnSpan, int surplus) {
            Integer[] clone = ArrayUtils.clone(columnSpan);
            if (clone.length <= 0) {
                return clone;
            }
            while (surplus > 0) {
                for (int i = clone.length - 1; i >= 0; i++) {
                    if (surplus <= 0) {
                        return clone;
                    }
                    clone[i] += 1;
                    surplus--;
                }
            }
            return clone;

        }

        /**
         * Distribute remaining cell span surplus one by one starting from the
         * left (array start)
         *
         * @param columnSpan
         * @param surplus
         * @return
         */
        public static Integer[] distributeSurplusLeft(Integer[] columnSpan, int surplus) {
            Integer[] clone = ArrayUtils.clone(columnSpan);
            if (clone.length <= 0) {
                return clone;
            }
            while (surplus > 0) {
                for (int i = 0; i < clone.length; i++) {
                    if (surplus <= 0) {
                        return clone;
                    }
                    clone[i] += 1;
                    surplus--;
                }
            }

            return clone;
        }

        /**
         * Distribute remaining cell span surplus one by one starting from the
         * left and oscillate to the left (array ends)
         *
         * @param columnSpan
         * @param surplus
         * @return
         */
        public static Integer[] distributeSurplusLeftRight(Integer[] columnSpan, int surplus) {
            Integer[] clone = ArrayUtils.clone(columnSpan);
            boolean odd = clone.length % 2 == 1;
            int center = clone.length / 2;
            if (odd) {
                center++;
            }
            if (clone.length <= 0) {
                return clone;
            }
            while (surplus > 0) {
                for (int i = 0; i < center; i++) {
                    if (surplus <= 0) {
                        return clone;
                    }
                    clone[i] += 1;
                    surplus--;

                    if (surplus <= 0) {
                        return clone;
                    }
                    int endIndex = clone.length - i - 1;
                    clone[endIndex] += 1;
                    surplus--;

                }
                if (odd && surplus > 0) {
                    clone[center] += 1;
                    surplus--;

                }
            }

            return clone;
        }

        /**
         * Distribute remaining cell span surplus one by one starting from the
         * left and oscillate to the left (array ends)
         *
         * @param columnSpan
         * @param surplus
         * @return
         */
        public static Integer[] distributeSurplusRightLeft(Integer[] columnSpan, int surplus) {
            Integer[] clone = ArrayUtils.clone(columnSpan);
            boolean odd = clone.length % 2 == 1;
            int center = clone.length / 2;
            if (odd) {
                center++;
            }
            if (clone.length <= 0) {
                return clone;
            }
            while (surplus > 0) {
                for (int i = 0; i < center; i++) {
                    if (surplus <= 0) {
                        return clone;
                    }
                    int endIndex = clone.length - i - 1;
                    clone[endIndex] += 1;
                    surplus--;

                    if (surplus <= 0) {
                        return clone;
                    }
                    clone[i] += 1;
                    surplus--;
                }
                if (odd && surplus > 0) {
                    clone[center] += 1;
                    surplus--;
                }
            }

            return clone;
        }
    }
}
