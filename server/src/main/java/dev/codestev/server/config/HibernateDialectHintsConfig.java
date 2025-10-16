package dev.codestev.server.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(HibernateDialectHints.class)
class HibernateDialectHintsConfig {}

class HibernateDialectHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    hints.reflection().registerType(
      TypeReference.of("org.hibernate.community.dialect.SQLiteDialect"),
      MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
    );
  }
}
