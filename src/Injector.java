import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 *
 * class Injector осуществяет внедрение зависимостей в любой объект , который содержит поля,
 * помеченные аннотацией @AutoInjectable
 */

class Injector<T> {
    // ссылка на конфигурационный объект
    private Properties properties;

    Injector(String pathToPropertiesFile) throws IOException {
        // инициализируем объект конфигурации
        properties = new Properties();
        properties.load(new FileInputStream(new File(pathToPropertiesFile)));
    }

    /**
     * inject принимает произвольный объект, исследует его на наличие полей с аннотацией AutoInjectable.
     * Если такое поле есть, смотрим его тип и ищем реализацию в файле inj.properties.

     * @param obj объект любого класса
     * @return возвращает объект с иницализированными палями с аннотацией AutoInjectable
     */
    T inject(T obj) throws IOException, IllegalAccessException, InstantiationException {
        // ссылка на зависимсоть в исследуемом классе
        Class dependency;

        Class cl = obj.getClass();

        // получаем список всех полей в объекте obj
        Field[] fields = cl.getDeclaredFields();
        for (Field field: fields){
            // проверяем, еслть ли аннотация AutoInjectable в исследуемом поле
            Annotation a = field.getAnnotation(AutoInjectable.class);
            if (a != null){
                /**
                 * получаем тип поля.
                 * Метод toString в данном случаем возвращает строку в виде "тип полное_имя",
                 * поэтому нам нужен первый элемент массива строк
                 */
                String[] fieldType = field.getType().toString().split(" ");
                // получаем из объекта конфигурации имя класса-зависимотсти
                String equalsClassName = properties.getProperty(fieldType[1], null);
                if (equalsClassName != null){
                    try {
                        dependency = Class.forName(equalsClassName);
                    } catch (ClassNotFoundException e){
                        System.out.println("Not found class for " + equalsClassName);
                        continue;
                    }
                    /** так так поля помечены модификатором private, для именения поля
                     * необходимо вызвать setAccessible с параметром true
                     */
                    field.setAccessible(true);
                    // инициализируем поле объектом, указанным в конфигурации
                    field.set(obj, dependency.newInstance());
                }
                else
                    System.out.println("Not found properties for field type " + fieldType[1]);
            }
        }
        return obj;
    }
}
