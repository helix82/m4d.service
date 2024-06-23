package example;

import java.security.MessageDigest;
import java.security.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.security.Security.getProviders;

public class AllHashAlgorithms {

    public static void main(String[] args) throws Exception {
        List<String> algorithms = getAvailableAlgorithms();
        // выводим алгоритмы и соответствующие хэш-значения
        algorithms.forEach(System.out::println);
    }

    public static List<String> getAvailableAlgorithms()
    {
        final String digestClassName = MessageDigest.class.getSimpleName();
        final String aliasPrefix = "Alg.Alias." + digestClassName + ".";

        return Arrays.stream(getProviders())
                .flatMap(prov -> {
                    final Set<String> algorithms = new HashSet<>(0);

                    prov.getServices().stream()
                            .filter(s -> digestClassName.equalsIgnoreCase(s.getType()))
                            .map(Provider.Service::getAlgorithm)
                            .collect(Collectors.toCollection(() -> algorithms));

                    prov.keySet().stream()
                            .map(Object::toString)
                            .filter(k -> k.startsWith(aliasPrefix))
                            .map(k -> String.format("\"%s\" -> \"%s\"", k.substring(aliasPrefix.length()), prov.get(k).toString()))
                            .collect(Collectors.toCollection(() -> algorithms));

                    return algorithms.stream();
                })
                .sorted(String::compareTo)
                .collect(Collectors.toList());
    }
}