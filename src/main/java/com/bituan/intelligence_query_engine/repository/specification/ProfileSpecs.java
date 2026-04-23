package com.bituan.intelligence_query_engine.repository.specification;

import com.bituan.intelligence_query_engine.exception.BadRequest;
import com.bituan.intelligence_query_engine.model.Profile;
import org.springframework.data.jpa.domain.Specification;

public class ProfileSpecs {
    public Specification<Profile> isGender (String gender) {
        if (gender != null && gender.isBlank()) {
            throw new BadRequest("Missing or empty parameter");
        }

        return (root, query, builder) -> gender == null ? null : builder.equal(root.get("gender"), gender);
    }

    public Specification<Profile> isAgeGroup (String ageGroup) {
        if (ageGroup != null && ageGroup.isBlank()) {
            throw new BadRequest("Missing or empty parameter");
        }

        return (root, query, builder) -> ageGroup == null ? null : builder.equal(root.get("ageGroup"), ageGroup);
    }

    public Specification<Profile> isCountryId (String countryId) {
        if (countryId != null && countryId.isBlank()) {
            throw new BadRequest("Missing or empty parameter");
        }

        return (root, query, builder) -> countryId == null ? null : builder.equal(root.get("countryId"), countryId);
    }

    public Specification<Profile> ageLessThan (Integer age) {
        return (root, query, builder) -> age == null ? null : builder.lessThan(root.get("age"), age);
    }

    public Specification<Profile> ageGreaterThan (Integer age) {
        return (root, query, builder) -> age == null ? null : builder.greaterThan(root.get("age"), age);
    }

    public Specification<Profile> ageLessThanOrEqualTo (Integer age) {
        return (root, query, builder) -> age == null ? null : builder.lessThanOrEqualTo(root.get("age"), age);
    }

    public Specification<Profile> ageGreaterThanOrEqualTo (Integer age) {
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
