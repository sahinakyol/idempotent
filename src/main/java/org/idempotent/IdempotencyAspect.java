package org.idempotent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.idempotent.datasource.DataSource;
import org.idempotent.ds.BloomFilter;
import org.idempotent.ds.MurmurHash3;

/**
 * Aspect for handling idempotency of method calls annotated with {@link Idempotent}.
 */
@Aspect
public class IdempotencyAspect {

    private final DataSource dataSource;
    private final BloomFilter bloomFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor for IdempotencyAspect.
     *
     * @param dataSource the data source to store idempotency keys.
     * @param bloomFilter the bloom filter to quickly check for existing keys.
     */
    public IdempotencyAspect(DataSource dataSource, BloomFilter bloomFilter) {
        this.dataSource = dataSource;
        this.bloomFilter = bloomFilter;
    }

    /**
     * Around advice to handle idempotent method calls.
     *
     * @param joinPoint the join point representing the method call.
     * @param idempotent the Idempotent annotation of the method.
     * @throws Throwable if the method execution throws an exception.
     */
    @Around("@annotation(idempotent)")
    public void handleIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String key = hashString(joinPoint.getArgs());

        if (bloomFilter.mightContain(key)) {
            return;
        }

        if (Boolean.TRUE.equals(dataSource.get(key))) {
            return;
        }

        joinPoint.proceed();

        bloomFilter.add(key);
        dataSource.set(key, idempotent.ttl());
    }

    /**
     * Hashes the method arguments to create a unique key.
     *
     * @param args the method arguments.
     * @return the hashed string.
     */
    private String hashString(Object[] args) {
        try {
            String jsonString = objectMapper.writeValueAsString(args);
            return MurmurHash3.hash128(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash string", e);
        }
    }
}
