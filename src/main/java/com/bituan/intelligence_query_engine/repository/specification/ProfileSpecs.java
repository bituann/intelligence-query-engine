package com.bituan.intelligence_query_engine.repository.specification;

import com.bituan.intelligence_query_engine.model.Profile;
import org.springframework.data.jpa.domain.Specification;

public class ProfileSpecs {
    public Specification<Profile> isGender (String gender) {
        return (root, query, builder) -> gender == null ? null : builder.equal(root.get("gender"), gender);
    }

    public Specification<Profile> isAgeGroup (String ageGroup) {
        return (root, query, builder) -> ageGroup == null ? null : builder.equal(root.get("age_group"), ageGroup);
    }

    public Specification<Profile> isCountryId (String countryId) {
        return (root, query, builder) -> countryId == null ? null : builder.equal(root.get("country_id"), countryId);
    }

    public Specification<Profile> ageLessThan (Integer age) {
        return (root, query, builder) -> age == null ? null : builder.lessThan(root.get("age"), age);
    }

    public Specification<Profile> ageGreaterThan (Integer age) {
        return (root, query, builder) -> age == null ? null : builder.greaterThan(root.get("age"), age);
    }

    public Specification<Profile> genderProbabilityLessThan (Double genderProbability) {
        return (root, query, builder) -> genderProbability == null ? null : builder.lessThan(root.get("gender_probability"), genderProbability);
    }

    public Specification<Profile> genderProbabilityGreaterThan (Double genderProbability) {
        return (root, query, builder) -> genderProbability == null ? null : builder.greaterThan(root.get("gender_probability"), genderProbability);
    }

    public Specification<Profile> countryProbabilityLessThan (Double countryProbability) {
        return (root, query, builder) -> countryProbability == null ? null : builder.lessThan(root.get("country_probability"), countryProbability);
    }

    public Specification<Profile> countryProbabilityGreaterThan (Double countryProbability) {
        return (root, query, builder) -> countryProbability == null ? null : builder.greaterThan(root.get("country_probability"), countryProbability);
    }
}
