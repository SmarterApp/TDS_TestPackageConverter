package tds.testpackageconverter.converter;

public class BlueprintElementCounts {
    private String id;
    private int examItemCount;
    private int fieldTestItemCount;

    public BlueprintElementCounts(final String id) {
        this.id = id;
        this.examItemCount = 0;
        this.fieldTestItemCount = 0;
    }

    public void incrementExamItemCount() {
        this.examItemCount++;
    }

    public void incrementFieldTestItemCount() {
        this.fieldTestItemCount++;
    }

    public String getId() {
        return id;
    }

    public int getExamItemCount() {
        return examItemCount;
    }

    public int getFieldTestItemCount() {
        return fieldTestItemCount;
    }
}