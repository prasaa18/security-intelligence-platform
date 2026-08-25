package com.securityintel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
            new LocalDateTimeToDateConverter(),
            new DateToLocalDateTimeConverter()
        ));
    }

    @Bean
    public MongoTemplate mongoTemplate(
            org.springframework.data.mongodb.MongoDatabaseFactory mongoDatabaseFactory,
            org.springframework.data.mongodb.core.convert.MongoConverter mongoConverter) {

        MongoTemplate template = new MongoTemplate(mongoDatabaseFactory, mongoConverter);

        // Remove _class field from documents
        MappingMongoConverter converter =
                (MappingMongoConverter) template.getConverter();

        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        // Disable auto index creation to handle existing indexes
        template.setAutoIndexCreation(false);

        return template;
    }

    // Custom converters for LocalDateTime
    static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        @Override
        public Date convert(LocalDateTime source) {
            return Date.from(source.toInstant(ZoneOffset.UTC));
        }
    }

    static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        @Override
        public LocalDateTime convert(Date source) {
            return LocalDateTime.ofInstant(source.toInstant(), ZoneOffset.UTC);
        }
    }
}
