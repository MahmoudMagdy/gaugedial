package com.example.gaugedialinformation.exceptions

class ContextDeadException :
    IllegalAccessException("Accessing context while application already dead.")