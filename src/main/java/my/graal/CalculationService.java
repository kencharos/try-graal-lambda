package my.graal;

import javax.inject.Singleton;

/**
 * DI Sample
 */
@Singleton
public class CalculationService {

    public int calc(int a, int b) {
        return  a + b;
    }

}
