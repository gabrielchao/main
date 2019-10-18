package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.MutatorCommand;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final int MAX_HISTORY_SIZE = 20;

    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private AddressBook baseAddressBook;
    private AddressBook stagedAddressBook;
    private final HistoryManager historyManager;
    private final UserPrefs userPrefs;
    private final ObservableList<Person> stagedPersons; // Modifiable list containing current stagedAddressBook persons
    private final FilteredList<Person> filteredPersons; // Unmodifiable view for the UI linked to stagedPersons

    //Previous predicate variable to keep track of the predicate used by FindCommands
    private Predicate<Person> previousPredicate = PREDICATE_SHOW_ALL_PERSONS;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.baseAddressBook = new AddressBook(addressBook);
        this.stagedAddressBook = this.baseAddressBook.deepCopy();
        this.historyManager = new HistoryManager(MAX_HISTORY_SIZE);
        this.userPrefs = new UserPrefs(userPrefs);
        stagedPersons = FXCollections.observableArrayList();
        filteredPersons = new FilteredList<>(FXCollections.unmodifiableObservableList(stagedPersons));

        refreshStagedPersons();
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setStagedAddressBook(ReadOnlyAddressBook addressBook) {
        this.stagedAddressBook.resetData(addressBook);
        refreshStagedPersons();
    }

    @Override
    public ReadOnlyAddressBook getStagedAddressBook() {
        return stagedAddressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return stagedAddressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        stagedAddressBook.removePerson(target);
        refreshStagedPersons();
        refreshFilteredPersonList();
    }

    @Override
    public void addPerson(Person person) {
        stagedAddressBook.addPerson(person);
        refreshStagedPersons();
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        stagedAddressBook.setPerson(target, editedPerson);
    }

    /**
     * Returns an unmodifiable view of the full list of {@code Person} backed by {@code stagedPersons}
     */
    @Override
    public ObservableList<Person> getStagedPersonList() {
        return new FilteredList<>(FXCollections.unmodifiableObservableList(stagedPersons));
    }

    @Override
    public boolean hasStagedChanges() {
        return !baseAddressBook.equals(stagedAddressBook);
    }

    @Override
    public void commit(MutatorCommand command) {
        historyManager.pushRecord(command, baseAddressBook);
        changeBaseTo(stagedAddressBook);
    }

    @Override
    public void discardStagedChanges() {
        stagedAddressBook = baseAddressBook.deepCopy();
        refreshStagedPersons();
    }

    @Override
    public List<HistoryRecord> revertTo(HistoryRecord record) throws NoSuchElementException {
        List<HistoryRecord> poppedRecords = historyManager.popRecordsTo(record);
        changeBaseTo(record.getCopyOfAddressBook());
        return poppedRecords;
    }

    @Override
    public ObservableList<HistoryRecord> getHistory() {
        return historyManager.asUnmodifiableObservableList();
    }

    private void changeBaseTo(AddressBook addressBook) {
        baseAddressBook = addressBook;
        stagedAddressBook = baseAddressBook.deepCopy();
        refreshStagedPersons();
    }

    private void refreshStagedPersons() {
        stagedPersons.setAll(stagedAddressBook.getPersonList());
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by {@code stagedPersons}
     */
    @Override
    public FilteredList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    /**
     * Refreshes the filter of the filtered patient list to filter by the given {@code predicate}.
     * This will update the indexes of patients if a patient outside of the filtered list has been removed.
     */
    private void refreshFilteredPersonList() {
        //In order to refresh predicate, need to reset it to show all persons first.
        //Otherwise it will not change anything
        filteredPersons.setPredicate(PREDICATE_SHOW_ALL_PERSONS);
        filteredPersons.setPredicate(previousPredicate);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        previousPredicate = predicate;
        filteredPersons.setPredicate(predicate);
    }

    /**
     * Returns true if the current state of this {@code ModelManager} is the same as {@code obj}.
     * It does NOT take into account {@code baseAddressBook} or {@code historyManager}.
     */
    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return stagedAddressBook.equals(other.stagedAddressBook)
                && userPrefs.equals(other.userPrefs)
                && filteredPersons.equals(other.filteredPersons);
    }

}
