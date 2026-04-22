package com.bituan.intelligence_query_engine.repository.specification;

import com.bituan.intelligence_query_engine.model.Profile;
import org.springframework.data.jpa.domain.Specification;

public class ProfileSpecs {
    public Specification<Profile> isGender (String gender) {
        return (root, query, builder) -> gender == null || gender.isBlank() ? null : builder.equal(root.get("gender"), gender);
    }

    public Specification<Profile> isAgeGroup (String ageGroup) {
        return (root, query, builder) -> ageGroup == null || ageGroup.isBlank() ? null : builder.equal(root.get("ageGroup"), ageGroup);
    }

    public Specification<Profile> isCountryId (String countryId) {
        return (root, query, builder) -> countryId == null || countryId.isBlank() ? null : builder.equal(root.get("countryId"), countryId);
    }

    public Specification<Profile> ageLessThan (Integer age) {
        return (root, query, builder) -> age == null ? null : builder.lessThanOrEqualTo(root.get("age"), age);
    }

    public Specification<Profile> ageGreaterThan (Integer age) {
        return (root, query, builder) -> age == null ? null : builder.greaterThanOrEqualTo(root.get("age"), age);
    }

    public Specification<Profile> genderProbabilityLessThan (Double genderProbability) {
        return (root, query, builder) -> genderProbability == null ? null : builder.lessThanOrEqualTo(root.get("genderProbability"), genderProbability);
    }

    public Specification<Profile> genderProbabilityGreaterThan (Double genderProbability) {
        return (root, query, builder) -> genderProbability == null ? null : builder.greaterThanOrEqualTo(root.get("genderProbability"), genderProbability);
    }

    public Specification<Profile> countryProbabilityLessThan (Double countryProbability) {
        return (root, query, builder) -> countryProbability == null ? null : builder.lessThanOrEqualTo(root.get("countryProbability"), countryProbability);
    }

    public Specification<Profile> countryProbabilityGreaterThan (Double countryProbability) {
        return (root, query, builder) -> countryProbability == null ? null : builder.greaterThanOrEqualTo(root.get("countryProbability"), countryProbability);
    }
}
