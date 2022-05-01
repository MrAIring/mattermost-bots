package io.github.mrairing.mattermost.configuration.native

import com.oracle.svm.core.annotate.AutomaticFeature
import com.oracle.svm.hosted.FeatureImpl
import io.micronaut.core.graal.AutomaticFeatureUtils
import org.graalvm.nativeimage.hosted.Feature
import org.jooq.Record

@AutomaticFeature
class JooqFeature : Feature {
    override fun beforeAnalysis(access: Feature.BeforeAnalysisAccess) {
        (access as FeatureImpl.FeatureAccessImpl)
            .findSubclasses(Record::class.java)
            .filter { it.canonicalName.contains("io.github.mrairing") }
            .forEach {
                AutomaticFeatureUtils.registerClassForRuntimeReflectionAndReflectiveInstantiation(
                    access,
                    it.canonicalName
                )
            }
    }
}