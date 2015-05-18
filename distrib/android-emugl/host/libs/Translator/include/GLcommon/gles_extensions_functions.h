// Auto-generated with: android/scripts/gen-entries.py --mode=functions distrib/android-emugl/host/libs/Translator/GLcommon/gles_extensions.entries --output=distrib/android-emugl/host/libs/Translator/include/GLcommon/gles_extensions_functions.h
// DO NOT EDIT THIS FILE

#ifndef GLES_EXTENSIONS_FUNCTIONS_H
#define GLES_EXTENSIONS_FUNCTIONS_H

#define LIST_GLES_EXTENSIONS_FUNCTIONS(X) \
  X(GLboolean, glIsRenderbufferEXT, (GLuint renderbuffer)) \
  X(void, glBindRenderbufferEXT, (GLenum target, GLuint renderbuffer)) \
  X(void, glDeleteRenderbuffersEXT, (GLsizei n, const GLuint * renderbuffers)) \
  X(void, glGenRenderbuffersEXT, (GLsizei n, GLuint * renderbuffers)) \
  X(void, glRenderbufferStorageEXT, (GLenum target, GLenum internalformat, GLsizei width, GLsizei height)) \
  X(void, glGetRenderbufferParameterivEXT, (GLenum target, GLenum pname, GLint * params)) \
  X(GLboolean, glIsFramebufferEXT, (GLuint framebuffer)) \
  X(void, glBindFramebufferEXT, (GLenum target, GLuint framebuffer)) \
  X(void, glDeleteFramebuffersEXT, (GLsizei n, const GLuint * framebuffers)) \
  X(void, glGenFramebuffersEXT, (GLsizei n, GLuint * framebuffers)) \
  X(GLenum, glCheckFramebufferStatusEXT, (GLenum target)) \
  X(void, glFramebufferTexture1DEXT, (GLenum target, GLenum attachment, GLenum textarget, GLuint texture, GLint level)) \
  X(void, glFramebufferTexture2DEXT, (GLenum target, GLenum attachment, GLenum textarget, GLuint texture, GLint level)) \
  X(void, glFramebufferTexture3DEXT, (GLenum target, GLenum attachment, GLenum textarget, GLuint texture, GLint level, GLint zoffset)) \
  X(void, glFramebufferRenderbufferEXT, (GLenum target, GLenum attachment, GLenum renderbuffertarget, GLuint renderbuffer)) \
  X(void, glGetFramebufferAttachmentParameterivEXT, (GLenum target, GLenum attachment, GLenum pname, GLint * params)) \
  X(void, glGenerateMipmapEXT, (GLenum target)) \


#endif  // GLES_EXTENSIONS_FUNCTIONS_H
