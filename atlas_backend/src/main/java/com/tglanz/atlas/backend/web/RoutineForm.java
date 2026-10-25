package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import com.tglanz.atlas.backend.routine.domain.Section;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/** The editable, ordered values submitted by a routine's HTML form. */
public final class RoutineForm {
  @NotBlank(message = "Enter a routine name")
  @Size(max = 200, message = "Routine names must have at most 200 characters")
  private String name = "";

  @Size(max = 20000, message = "Routine descriptions must have at most 20000 characters")
  private String description = "";

  @Valid private List<SectionForm> sections = new ArrayList<>();

  public static RoutineForm from(RoutineContent content) {
    RoutineForm form = new RoutineForm();
    form.name = content.name();
    form.description = content.description();
    for (Section section : content.sections()) {
      SectionForm values = new SectionForm();
      values.name = section.name();
      values.description = section.description();
      values.movements = new ArrayList<>(section.movements());
      form.sections.add(values);
    }
    return form;
  }

  RoutineContent toContent() {
    return new RoutineContent(
        name, description, sections.stream().map(SectionForm::toSection).toList());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<SectionForm> getSections() {
    return sections;
  }

  public void setSections(List<SectionForm> sections) {
    this.sections = sections == null ? new ArrayList<>() : new ArrayList<>(sections);
  }

  public static final class SectionForm {
    @NotBlank(message = "Enter a name for every section")
    @Size(max = 200, message = "Section names must have at most 200 characters")
    private String name = "";

    @Size(max = 20000, message = "Section descriptions must have at most 20000 characters")
    private String description = "";

    private List<@NotBlank(message = "Choose a movement for every entry") String> movements =
        new ArrayList<>();

    Section toSection() {
      return new Section(name, description, movements);
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public List<String> getMovements() {
      return movements;
    }

    public void setMovements(List<String> movements) {
      this.movements = movements == null ? new ArrayList<>() : new ArrayList<>(movements);
    }
  }
}
