package de.herrmann_engel.rbv.utils

class CompareDataObjects {
    fun areTheySame(
        object1: Any?,
        object2: Any?
    ): Boolean {
        if (object1 == null || object2 == null) {
            return object1 == object2
        }
        if (object1::class != object2::class) {
            return false
        }
        for (field in object1.javaClass.fields) {
            if (!field.type.isPrimitive) {
                if (!areTheySame(field.get(object1), field.get(object2))) {
                    return false
                }
            } else if (field.get(object1) != field.get(object2)) {
                return false
            }
        }
        return true
    }
}
