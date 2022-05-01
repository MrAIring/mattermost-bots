package io.github.mrairing.mattermost.configuration

import io.micronaut.configuration.jooq.JooqConfigurationFactory
import io.micronaut.configuration.jooq.JsonConverterProvider
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.EachBean
import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Replaces
import io.micronaut.core.annotation.Nullable
import io.micronaut.inject.qualifiers.Qualifiers
import io.r2dbc.spi.ConnectionFactory
import org.jooq.Configuration
import org.jooq.ConverterProvider
import org.jooq.DiagnosticsListenerProvider
import org.jooq.ExecuteListenerProvider
import org.jooq.ExecutorProvider
import org.jooq.MetaProvider
import org.jooq.RecordListenerProvider
import org.jooq.RecordMapperProvider
import org.jooq.RecordUnmapperProvider
import org.jooq.SQLDialect
import org.jooq.TransactionListenerProvider
import org.jooq.TransactionProvider
import org.jooq.VisitListenerProvider
import org.jooq.conf.Settings
import org.jooq.impl.DefaultConfiguration

@Replaces(factory = JooqConfigurationFactory::class)
@Factory
class R2dbcJooqConfigurationFactory {
    /**
     * Creates jOOQ [Configuration]. It will configure it with available jOOQ provider beans with the same
     * qualifier.
     *
     * @param name                   The data source name
     * @param connectionFactory      The [ConnectionFactory]
     * @param transactionProvider    The transaction provider
     * @param settings               The settings
     * @param executorProvider       The executor provider
     * @param recordMapperProvider   The record mapper provider
     * @param recordUnmapperProvider The record unmapper provider
     * @param metaProvider           The metadata provider
     * @param converterProvider      The converter provider
     * @param ctx                    The [ApplicationContext]
     * @return A [Configuration]
     */
    @EachBean(ConnectionFactory::class)
    fun jooqConfiguration(
        @Parameter name: String?,
        connectionFactory: ConnectionFactory?,
        @Parameter @Nullable transactionProvider: TransactionProvider?,
        @Parameter @Nullable settings: Settings?,
        @Parameter @Nullable executorProvider: ExecutorProvider?,
        @Parameter @Nullable recordMapperProvider: RecordMapperProvider?,
        @Parameter @Nullable recordUnmapperProvider: RecordUnmapperProvider?,
        @Parameter @Nullable metaProvider: MetaProvider?,
        @Parameter @Nullable converterProvider: ConverterProvider?,
        ctx: ApplicationContext
    ): Configuration {
        val configuration = DefaultConfiguration()
        val properties = ctx.findBean(R2dbcJooqConfigurationProperties::class.java, Qualifiers.byName(name))
            .orElseGet { R2dbcJooqConfigurationProperties() }
        configuration.setSQLDialect(getSqlDialect(properties))
        configuration.setConnectionFactory(connectionFactory)
        if (transactionProvider != null) {
            configuration.setTransactionProvider(transactionProvider)
        }
        if (settings != null) {
            configuration.setSettings(settings)
        }
        if (executorProvider != null) {
            configuration.setExecutorProvider(executorProvider)
        }
        if (recordMapperProvider != null) {
            configuration.setRecordMapperProvider(recordMapperProvider)
        }
        if (recordUnmapperProvider != null) {
            configuration.setRecordUnmapperProvider(recordUnmapperProvider)
        }
        if (metaProvider != null) {
            configuration.setMetaProvider(metaProvider)
        }
        if (converterProvider != null) {
            configuration.set(converterProvider)
        } else if (properties.jsonConverterEnabled) {
            ctx.findBean(JsonConverterProvider::class.java)
                .ifPresent { newConverterProvider: JsonConverterProvider? ->
                    configuration.set(
                        newConverterProvider
                    )
                }
        }
        configuration.setExecuteListenerProvider(
            *ctx.getBeansOfType(ExecuteListenerProvider::class.java, Qualifiers.byName(name))
                .toTypedArray()
        )
        configuration.setRecordListenerProvider(
            *ctx.getBeansOfType(RecordListenerProvider::class.java, Qualifiers.byName(name))
                .toTypedArray()
        )
        configuration.setVisitListenerProvider(
            *ctx.getBeansOfType(VisitListenerProvider::class.java, Qualifiers.byName(name))
                .toTypedArray()
        )
        configuration.setTransactionListenerProvider(
            *ctx.getBeansOfType(TransactionListenerProvider::class.java, Qualifiers.byName(name))
                .toTypedArray()
        )
        configuration.setDiagnosticsListenerProvider(
            *ctx.getBeansOfType(DiagnosticsListenerProvider::class.java, Qualifiers.byName(name))
                .toTypedArray()
        )
        return configuration
    }

    private fun getSqlDialect(properties: R2dbcJooqConfigurationProperties): SQLDialect {
        var sqlDialect = properties.sqlDialect
        if (sqlDialect == null) {
            sqlDialect = SQLDialect.DEFAULT
        }
        return sqlDialect
    }
}


@EachProperty(value = "jooq.r2dbc-datasources")
class R2dbcJooqConfigurationProperties {
    /**
     * SQL dialect to use. If `null`, will be detected automatically.
     *
     * @return SQL dialect
     */
    /**
     * SQL dialect to use. Will be detected automatically by default.
     *
     * @param sqlDialect SQL dialect
     */
    var sqlDialect: SQLDialect? = null
    /**
     * If enable [JacksonConverterProvider] bean to use Jackson for JSON and JSONB types.
     *
     * @return boolean
     */
    /**
     * Set if enable [JacksonConverterProvider] bean to use Jackson for JSON and JSONB types.
     *
     * @param jacksonConverterEnabled Enable Jackson
     */
    var jsonConverterEnabled = false
}